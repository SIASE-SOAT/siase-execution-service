package br.com.fiap.siase.infrastructure.adapters.input;

import com.fasterxml.jackson.databind.JsonNode;
import br.com.fiap.siase.application.usecase.CompensarExecucaoUseCase;
import br.com.fiap.siase.application.usecase.EnfileirarOsUseCase;
import br.com.fiap.siase.domain.model.EventoExecucao;
import br.com.fiap.siase.infrastructure.observability.SqsCorrelationId;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Consumer da execution-queue: ORCAMENTO_APROVADO enfileira a OS;
 * OS_CANCELADA dispara a compensação (remove da fila e confirma EXECUCAO_CANCELADA).
 */
@Component
public class ExecutionQueueConsumer {

  private static final Logger log = LoggerFactory.getLogger(ExecutionQueueConsumer.class);

  private final SqsClient sqs;
  private final ObjectMapper mapper;
  private final EnfileirarOsUseCase enfileirar;
  private final CompensarExecucaoUseCase compensar;
  private final MeterRegistry metrics;
  private final String queueUrl;

  public ExecutionQueueConsumer(SqsClient sqs, ObjectMapper mapper,
                                EnfileirarOsUseCase enfileirar, CompensarExecucaoUseCase compensar,
                                MeterRegistry metrics,
                                @Value("${app.sqs.execution-url}") String queueUrl) {
    this.sqs = sqs;
    this.mapper = mapper;
    this.enfileirar = enfileirar;
    this.compensar = compensar;
    this.metrics = metrics;
    this.queueUrl = queueUrl;
  }

  @Scheduled(fixedDelayString = "${app.sqs.poll-delay-ms:5000}")
  public void poll() {
    var response = sqs.receiveMessage(ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(10)
            .waitTimeSeconds(20)
            .messageAttributeNames("All")
            .build());

    for (Message message : response.messages()) {
      try {
        SqsCorrelationId.mdcFrom(message);
        EventoExecucao evento = mapper.readValue(message.body(), EventoExecucao.class);
        dispatch(evento);
        sqs.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl).receiptHandle(message.receiptHandle()).build());
      } catch (Exception e) {
        // não deleta: volta à fila; após maxReceiveCount vai para a DLQ
        log.error("Falha ao processar mensagem {}: {}", message.messageId(), e.getMessage(), e);
      } finally {
        MDC.clear();
      }
    }
  }

  private void dispatch(EventoExecucao evento) {
    metrics.counter("siase_saga_eventos_total", "servico", "execution",
            "evento", evento.tipoEvento(), "direcao", "consumido").increment();
    switch (evento.tipoEvento()) {
      case EventoExecucao.ORCAMENTO_APROVADO -> enfileirar.executar(evento.osId(),
              evento.numeroOs(), campoTexto(evento.payload(), "clienteNome"),
              campoTexto(evento.payload(), "veiculo"), servicos(evento.payload()));
      case EventoExecucao.OS_CANCELADA -> compensar.executar(evento.osId());
      default -> log.warn("Evento desconhecido ignorado: {}", evento.tipoEvento());
    }
  }

  private String campoTexto(String payload, String campo) {
    JsonNode node = payload(payload);
    JsonNode valor = node == null ? null : node.get(campo);
    return valor != null && valor.isTextual() ? valor.asText() : null;
  }

  private List<String> servicos(String payload) {
    JsonNode node = payload(payload);
    if (node == null) return List.of();
    JsonNode itens = node.has("servicos") ? node.get("servicos") : node.get("itensServico");
    if (itens == null || !itens.isArray()) return List.of();
    List<String> nomes = new ArrayList<>();
    for (JsonNode item : itens) {
      if (item.isTextual()) {
        nomes.add(item.asText());
      } else if (item.hasNonNull("descricao")) {
        nomes.add(item.get("descricao").asText());
      } else if (item.hasNonNull("servicoNome")) {
        nomes.add(item.get("servicoNome").asText());
      } else if (item.hasNonNull("nome")) {
        nomes.add(item.get("nome").asText());
      }
    }
    return nomes;
  }

  private JsonNode payload(String payload) {
    try {
      return payload == null || payload.isBlank() ? null : mapper.readTree(payload);
    } catch (Exception e) {
      log.warn("Payload do evento não é JSON válido: {}", payload);
      return null;
    }
  }
}


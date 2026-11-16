package br.com.fiap.siase.infrastructure.adapters.output.sqs;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.domain.model.EventoExecucao;
import br.com.fiap.siase.infrastructure.observability.SqsCorrelationId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/** Publica eventos de execução na os-updates-queue (consumida pelo os-service). */
@Component
public class SqsEventoPublicadorAdapter implements EventoPublicadorPort {

  private static final Logger log = LoggerFactory.getLogger(SqsEventoPublicadorAdapter.class);

  private final SqsClient sqs;
  private final ObjectMapper mapper;
  private final MeterRegistry metrics;
  private final String osUpdatesQueueUrl;

  public SqsEventoPublicadorAdapter(SqsClient sqs, ObjectMapper mapper, MeterRegistry metrics,
                                    @Value("${app.sqs.os-updates-url}") String osUpdatesQueueUrl) {
    this.sqs = sqs;
    this.mapper = mapper;
    this.metrics = metrics;
    this.osUpdatesQueueUrl = osUpdatesQueueUrl;
  }

  @Override
  public void publicar(EventoExecucao evento) {
    SendMessageRequest request = SqsCorrelationId.withCorrelationId(
                    SendMessageRequest.builder()
                            .queueUrl(osUpdatesQueueUrl)
                            .messageBody(serializar(evento)))
            .build();
    sqs.sendMessage(request);
    metrics.counter("siase_saga_eventos_total", "servico", "execution",
            "evento", evento.tipoEvento(), "direcao", "produzido").increment();
    log.info("Evento {} da OS {} publicado", evento.tipoEvento(), evento.numeroOs());
  }

  private String serializar(EventoExecucao evento) {
    try {
      return mapper.writeValueAsString(evento);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Falha ao serializar evento " + evento.tipoEvento(), e);
    }
  }
}


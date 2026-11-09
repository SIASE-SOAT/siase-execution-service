package br.com.fiap.siase.infrastructure.messaging;

import br.com.fiap.siase.application.usecase.CancelarExecucaoUC;
import br.com.fiap.siase.application.usecase.EnfileirarOsUC;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsExecutionConsumer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final EnfileirarOsUC enfileirarOsUC;
    private final CancelarExecucaoUC cancelarExecucaoUC;

    @Value("${aws.sqs.queue-url-execution}")
    private String executionQueueUrl;

    @Scheduled(fixedDelayString = "${aws.sqs.poll-interval-ms:5000}")
    public void consumir() {
        try {
            var messages = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                    .queueUrl(executionQueueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5)
                    .build()).messages();

            for (var msg : messages) {
                try {
                    processar(msg.body());
                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(executionQueueUrl)
                            .receiptHandle(msg.receiptHandle())
                            .build());
                } catch (Exception e) {
                    log.error("Erro ao processar mensagem execution: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao consumir fila execution: {}", e.getMessage());
        }
    }

    private void processar(String body) throws Exception {
        JsonNode node = objectMapper.readTree(body);
        String tipo = node.get("tipo").asText();

        switch (tipo) {
            case "ORCAMENTO_APROVADO" -> enfileirarOsUC.executar(
                    node.get("osId").asText(),
                    node.get("osNumero").asText(),
                    node.has("clienteNome") ? node.get("clienteNome").asText() : "",
                    node.has("veiculoPlaca") ? node.get("veiculoPlaca").asText() : "",
                    node.has("veiculoModelo") ? List.of(node.get("veiculoModelo").asText()) : List.of()
            );
            case "OS_CANCELADA" -> cancelarExecucaoUC.executar(
                    node.get("osId").asText(),
                    node.has("motivo") ? node.get("motivo").asText() : "OS cancelada"
            );
            default -> log.debug("Tipo de evento execution ignorado: {}", tipo);
        }
    }
}

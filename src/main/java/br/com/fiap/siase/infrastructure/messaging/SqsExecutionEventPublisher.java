package br.com.fiap.siase.infrastructure.messaging;

import br.com.fiap.siase.domain.port.ExecutionEventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsExecutionEventPublisher implements ExecutionEventPublisherPort {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-url-os-updates}")
    private String osUpdatesQueueUrl;

    @Override
    public void publicarExecucaoIniciada(String osNumero, String osId) {
        publicar(Map.of("tipo", "EXECUCAO_INICIADA", "osNumero", osNumero, "osId", osId));
        log.info("Evento EXECUCAO_INICIADA publicado para OS {}", osNumero);
    }

    @Override
    public void publicarExecucaoFinalizada(String osNumero, String osId) {
        publicar(Map.of("tipo", "EXECUCAO_FINALIZADA", "osNumero", osNumero, "osId", osId));
        log.info("Evento EXECUCAO_FINALIZADA publicado para OS {}", osNumero);
    }

    private void publicar(Object payload) {
        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(osUpdatesQueueUrl)
                    .messageBody(objectMapper.writeValueAsString(payload))
                    .build());
        } catch (Exception e) {
            log.error("Falha ao publicar evento SQS: {}", e.getMessage(), e);
        }
    }
}

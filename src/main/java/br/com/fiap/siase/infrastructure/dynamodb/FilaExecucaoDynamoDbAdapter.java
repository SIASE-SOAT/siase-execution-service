package br.com.fiap.siase.infrastructure.dynamodb;

import br.com.fiap.siase.domain.enums.StatusExecucao;
import br.com.fiap.siase.domain.model.FilaExecucao;
import br.com.fiap.siase.domain.port.FilaExecucaoRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class FilaExecucaoDynamoDbAdapter implements FilaExecucaoRepositoryPort {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.table-fila:siase-execution-fila}")
    private String tableName;

    @Override
    public FilaExecucao save(FilaExecucao fila) {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("osId", AttributeValue.fromS(fila.getOsId()));
            item.put("osNumero", AttributeValue.fromS(fila.getOsNumero()));
            item.put("status", AttributeValue.fromS(fila.getStatus().name()));
            item.put("atualizadoEm", AttributeValue.fromS(LocalDateTime.now().toString()));
            if (fila.getClienteNome() != null) item.put("clienteNome", AttributeValue.fromS(fila.getClienteNome()));
            if (fila.getVeiculoPlaca() != null) item.put("veiculoPlaca", AttributeValue.fromS(fila.getVeiculoPlaca()));
            if (fila.getEnfileiradoEm() != null) item.put("enfileiradoEm", AttributeValue.fromS(fila.getEnfileiradoEm().toString()));
            if (fila.getIniciadoEm() != null) item.put("iniciadoEm", AttributeValue.fromS(fila.getIniciadoEm().toString()));
            if (fila.getFinalizadoEm() != null) item.put("finalizadoEm", AttributeValue.fromS(fila.getFinalizadoEm().toString()));
            if (fila.getItensServico() != null && !fila.getItensServico().isEmpty()) {
                item.put("itensServico", AttributeValue.fromSs(fila.getItensServico()));
            }
            dynamoDbClient.putItem(PutItemRequest.builder().tableName(tableName).item(item).build());
        } catch (Exception e) {
            log.error("Erro ao salvar fila no DynamoDB: {}", e.getMessage(), e);
        }
        return fila;
    }

    @Override
    public Optional<FilaExecucao> findByOsId(String osId) {
        try {
            var response = dynamoDbClient.getItem(GetItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of("osId", AttributeValue.fromS(osId)))
                    .build());
            if (!response.hasItem() || response.item().isEmpty()) return Optional.empty();
            return Optional.of(toFila(response.item()));
        } catch (Exception e) {
            log.warn("Erro ao buscar fila no DynamoDB: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<FilaExecucao> findAll() {
        try {
            return dynamoDbClient.scan(ScanRequest.builder().tableName(tableName).build())
                    .items().stream().map(this::toFila).toList();
        } catch (Exception e) {
            log.warn("Erro ao listar fila no DynamoDB: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void deleteByOsId(String osId) {
        try {
            dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of("osId", AttributeValue.fromS(osId)))
                    .build());
        } catch (Exception e) {
            log.warn("Erro ao deletar fila no DynamoDB: {}", e.getMessage());
        }
    }

    private FilaExecucao toFila(Map<String, AttributeValue> item) {
        return FilaExecucao.builder()
                .osId(item.get("osId").s())
                .osNumero(item.get("osNumero").s())
                .status(StatusExecucao.valueOf(item.get("status").s()))
                .clienteNome(item.containsKey("clienteNome") ? item.get("clienteNome").s() : null)
                .veiculoPlaca(item.containsKey("veiculoPlaca") ? item.get("veiculoPlaca").s() : null)
                .enfileiradoEm(item.containsKey("enfileiradoEm") ? LocalDateTime.parse(item.get("enfileiradoEm").s()) : null)
                .iniciadoEm(item.containsKey("iniciadoEm") ? LocalDateTime.parse(item.get("iniciadoEm").s()) : null)
                .finalizadoEm(item.containsKey("finalizadoEm") ? LocalDateTime.parse(item.get("finalizadoEm").s()) : null)
                .itensServico(item.containsKey("itensServico") ? item.get("itensServico").ss() : List.of())
                .build();
    }
}

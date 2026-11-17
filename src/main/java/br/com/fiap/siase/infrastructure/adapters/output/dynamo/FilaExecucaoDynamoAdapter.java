package br.com.fiap.siase.infrastructure.adapters.output.dynamo;

import br.com.fiap.siase.application.ports.FilaExecucaoRepositoryPort;
import br.com.fiap.siase.domain.model.ItemFilaExecucao;
import br.com.fiap.siase.domain.model.StatusExecucao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.*;

/**
 * Fila de execução no DynamoDB (banco NoSQL do serviço).
 * Tabela siase-<env>-execution-fila: hash key osId (S).
 */
@Component
public class FilaExecucaoDynamoAdapter implements FilaExecucaoRepositoryPort {

  private final DynamoDbClient dynamo;
  private final String tableName;

  public FilaExecucaoDynamoAdapter(DynamoDbClient dynamo,
                                   @Value("${app.dynamodb.fila-table}") String tableName) {
    this.dynamo = dynamo;
    this.tableName = tableName;
  }

  @Override
  public void salvar(ItemFilaExecucao item) {
    Map<String, AttributeValue> m = new HashMap<>();
    m.put("osId", AttributeValue.fromS(item.getOsId().toString()));
    m.put("numeroOs", AttributeValue.fromS(item.getNumeroOs()));
    texto(m, "clienteNome", item.getClienteNome());
    texto(m, "veiculo", item.getVeiculo());
    if (item.getItensServico() != null && !item.getItensServico().isEmpty()) {
      m.put("itensServico", AttributeValue.fromSs(item.getItensServico()));
    }
    texto(m, "mecanico", item.getMecanico());
    m.put("status", AttributeValue.fromS(item.getStatus().name()));
    m.put("entradaNaFila", AttributeValue.fromN(String.valueOf(item.getEntradaNaFila().toEpochMilli())));
    if (item.getInicioExecucao() != null)
      m.put("inicioExecucao", AttributeValue.fromN(String.valueOf(item.getInicioExecucao().toEpochMilli())));
    if (item.getFimExecucao() != null)
      m.put("fimExecucao", AttributeValue.fromN(String.valueOf(item.getFimExecucao().toEpochMilli())));
    dynamo.putItem(PutItemRequest.builder().tableName(tableName).item(m).build());
  }

  @Override
  public Optional<ItemFilaExecucao> buscarPorOsId(UUID osId) {
    GetItemResponse r = dynamo.getItem(GetItemRequest.builder()
            .tableName(tableName)
            .key(Map.of("osId", AttributeValue.fromS(osId.toString())))
            .build());
    return r.hasItem() ? Optional.of(fromItem(r.item())) : Optional.empty();
  }

  @Override
  public List<ItemFilaExecucao> listarNaFila() {
    return listarTodas().stream()
            .filter(i -> i.getStatus() == StatusExecucao.NA_FILA)
            .sorted(Comparator.comparing(ItemFilaExecucao::getEntradaNaFila))
            .toList();
  }

  @Override
  public List<ItemFilaExecucao> listarTodas() {
    ScanResponse r = dynamo.scan(ScanRequest.builder().tableName(tableName).build());
    return r.items().stream().map(this::fromItem)
            .sorted(Comparator.comparing(ItemFilaExecucao::getEntradaNaFila)).toList();
  }

  @Override
  public void remover(UUID osId) {
    dynamo.deleteItem(DeleteItemRequest.builder().tableName(tableName)
            .key(Map.of("osId", AttributeValue.fromS(osId.toString()))).build());
  }

  private void texto(Map<String, AttributeValue> item, String nome, String valor) {
    if (valor != null && !valor.isBlank()) item.put(nome, AttributeValue.fromS(valor));
  }

  private String texto(Map<String, AttributeValue> item, String nome) {
    return item.containsKey(nome) ? item.get(nome).s() : null;
  }

  private List<String> lista(Map<String, AttributeValue> item, String nome) {
    return item.containsKey(nome) && item.get(nome).hasSs()
            ? item.get(nome).ss() : List.of();
  }

  private ItemFilaExecucao fromItem(Map<String, AttributeValue> m) {
    Instant inicio = m.containsKey("inicioExecucao")
            ? Instant.ofEpochMilli(Long.parseLong(m.get("inicioExecucao").n())) : null;
    Instant fim = m.containsKey("fimExecucao")
            ? Instant.ofEpochMilli(Long.parseLong(m.get("fimExecucao").n())) : null;
    return ItemFilaExecucao.restaurar(
            UUID.fromString(m.get("osId").s()),
            m.get("numeroOs").s(),
            texto(m, "clienteNome"),
            texto(m, "veiculo"),
            lista(m, "itensServico"),
            texto(m, "mecanico"),
            StatusExecucao.valueOf(m.get("status").s()),
            Instant.ofEpochMilli(Long.parseLong(m.get("entradaNaFila").n())),
            inicio, fim);
  }
}
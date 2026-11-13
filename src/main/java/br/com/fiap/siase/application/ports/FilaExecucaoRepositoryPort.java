package br.com.fiap.siase.application.ports;

import br.com.fiap.siase.domain.model.ItemFilaExecucao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FilaExecucaoRepositoryPort {
  void salvar(ItemFilaExecucao item);
  Optional<ItemFilaExecucao> buscarPorOsId(UUID osId);
  List<ItemFilaExecucao> listarNaFila();
  List<ItemFilaExecucao> listarTodas();
  void remover(UUID osId);
}
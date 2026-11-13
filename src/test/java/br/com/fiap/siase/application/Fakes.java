package br.com.fiap.siase.application;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.application.ports.FilaExecucaoRepositoryPort;
import br.com.fiap.siase.application.ports.HistoricoRepositoryPort;
import br.com.fiap.siase.domain.model.EventoExecucao;
import br.com.fiap.siase.domain.model.ItemFilaExecucao;
import br.com.fiap.siase.domain.model.StatusExecucao;

import java.util.*;

public class Fakes {

  public static class FilaMemoria implements FilaExecucaoRepositoryPort {
    public final Map<UUID, ItemFilaExecucao> itens = new LinkedHashMap<>();
    public void salvar(ItemFilaExecucao item) { itens.put(item.getOsId(), item); }
    public Optional<ItemFilaExecucao> buscarPorOsId(UUID osId) { return Optional.ofNullable(itens.get(osId)); }
    public List<ItemFilaExecucao> listarNaFila() {
      return itens.values().stream().filter(i -> i.getStatus() == StatusExecucao.NA_FILA)
              .sorted(Comparator.comparing(ItemFilaExecucao::getEntradaNaFila)).toList();
    }
    public List<ItemFilaExecucao> listarTodas() { return new ArrayList<>(itens.values()); }
    public void remover(UUID osId) { itens.remove(osId); }
  }

  public static class PublicadorMemoria implements EventoPublicadorPort {
    public final List<EventoExecucao> eventos = new ArrayList<>();
    public void publicar(EventoExecucao evento) { eventos.add(evento); }
    public boolean publicou(String tipo) { return eventos.stream().anyMatch(e -> e.tipoEvento().equals(tipo)); }
  }

  public static class HistoricoMemoria implements HistoricoRepositoryPort {
    public final List<String> registros = new ArrayList<>();
    public void registrar(UUID osId, String evento, String detalhe) { registros.add(evento); }
  }
}

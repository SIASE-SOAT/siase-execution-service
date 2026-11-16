package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.application.ports.FilaExecucaoRepositoryPort;
import br.com.fiap.siase.application.ports.HistoricoRepositoryPort;
import br.com.fiap.siase.domain.model.EventoExecucao;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Compensação do Saga: OS_CANCELADA → cancela o item na fila e publica EXECUCAO_CANCELADA. */
@Service
public class CompensarExecucaoUseCase {
  private final FilaExecucaoRepositoryPort fila;
  private final EventoPublicadorPort publicador;
  private final HistoricoRepositoryPort historico;

  public CompensarExecucaoUseCase(FilaExecucaoRepositoryPort fila, EventoPublicadorPort publicador,
                                  HistoricoRepositoryPort historico) {
    this.fila = fila;
    this.publicador = publicador;
    this.historico = historico;
  }

  public void executar(UUID osId) {
    var opt = fila.buscarPorOsId(osId);
    if (opt.isEmpty()) {
      historico.registrar(osId, "EXECUCAO_CANCELADA_IGNORADA", "OS não estava na fila");
      return;
    }
    var item = opt.get();
    item.cancelar();
    fila.salvar(item);
    publicador.publicar(EventoExecucao.de(EventoExecucao.EXECUCAO_CANCELADA, osId, item.getNumeroOs(), "Execução cancelada por compensação"));
    historico.registrar(osId, "EXECUCAO_CANCELADA", "Cancelamento compensado");
  }
}

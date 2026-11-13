package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.FilaExecucaoRepositoryPort;
import br.com.fiap.siase.application.ports.HistoricoRepositoryPort;
import br.com.fiap.siase.domain.model.ItemFilaExecucao;
import org.springframework.stereotype.Service;

/** ORCAMENTO_APROVADO → insere a OS na fila de execução. Idempotente por osId. */
@Service
public class EnfileirarOsUseCase {
    private final FilaExecucaoRepositoryPort fila;
    private final HistoricoRepositoryPort historico;

    public EnfileirarOsUseCase(FilaExecucaoRepositoryPort fila, HistoricoRepositoryPort historico) {
        this.fila = fila;
        this.historico = historico;
    }

    public void executar(java.util.UUID osId, String numeroOs) {
        if (fila.buscarPorOsId(osId).isPresent()) return; // idempotência
        fila.salvar(new ItemFilaExecucao(osId, numeroOs));
        historico.registrar(osId, "ENFILEIRADA", "OS " + numeroOs + " aguardando início da execução");
    }
}

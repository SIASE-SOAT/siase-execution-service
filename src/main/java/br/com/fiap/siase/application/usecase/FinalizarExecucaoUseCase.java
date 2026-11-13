package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.application.ports.FilaExecucaoRepositoryPort;
import br.com.fiap.siase.application.ports.HistoricoRepositoryPort;
import br.com.fiap.siase.domain.model.EventoExecucao;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Mecânico finaliza → publica EXECUCAO_FINALIZADA (os-service → FINALIZADA → AGUARDANDO_PAGAMENTO). */
@Service
public class FinalizarExecucaoUseCase {
    private final FilaExecucaoRepositoryPort fila;
    private final EventoPublicadorPort publicador;
    private final HistoricoRepositoryPort historico;

    public FinalizarExecucaoUseCase(FilaExecucaoRepositoryPort fila, EventoPublicadorPort publicador,
                                    HistoricoRepositoryPort historico) {
        this.fila = fila;
        this.publicador = publicador;
        this.historico = historico;
    }

    public void executar(UUID osId) {
        var item = fila.buscarPorOsId(osId)
                .orElseThrow(() -> new IllegalArgumentException("OS não está em execução"));
        item.finalizar();
        fila.salvar(item);
        publicador.publicar(EventoExecucao.de(EventoExecucao.EXECUCAO_FINALIZADA, osId, item.getNumeroOs(), "Execução finalizada"));
        historico.registrar(osId, "EXECUCAO_FINALIZADA", "Serviço concluído");
    }
}

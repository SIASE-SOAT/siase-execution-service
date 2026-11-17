package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.application.ports.FilaExecucaoRepositoryPort;
import br.com.fiap.siase.application.ports.HistoricoRepositoryPort;
import br.com.fiap.siase.domain.model.EventoExecucao;
import org.springframework.stereotype.Service;


import java.util.UUID;

/** Mecânico inicia a execução → publica EXECUCAO_INICIADA (os-service → EM_EXECUCAO). */
@Service
public class IniciarExecucaoUseCase {
    private final FilaExecucaoRepositoryPort fila;
    private final EventoPublicadorPort publicador;
    private final HistoricoRepositoryPort historico;

    public IniciarExecucaoUseCase(FilaExecucaoRepositoryPort fila, EventoPublicadorPort publicador,
                                  HistoricoRepositoryPort historico) {
        this.fila = fila;
        this.publicador = publicador;
        this.historico = historico;
    }

    public void executar(UUID osId) {
        executar(osId, null);
    }

    public void executar(UUID osId, String mecanico) {
        var item = fila.buscarPorOsId(osId)
                .orElseThrow(() -> new IllegalArgumentException("OS não está na fila de execução"));
        item.iniciar(mecanico);
        fila.salvar(item);
        String payload = mecanico == null || mecanico.isBlank()
                ? "Execução iniciada"
                : "{\"mecanico\":\"" + mecanico.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
        publicador.publicar(EventoExecucao.de(EventoExecucao.EXECUCAO_INICIADA,
                osId, item.getNumeroOs(), payload));
        historico.registrar(osId, "EXECUCAO_INICIADA",
                mecanico == null ? "Mecânico iniciou a execução" : "Mecânico " + mecanico + " iniciou a execução");
    }
}

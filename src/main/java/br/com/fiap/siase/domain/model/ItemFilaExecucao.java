package br.com.fiap.siase.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Item na fila de execução de serviços (persistido no DynamoDB). */
public class ItemFilaExecucao {
    private final UUID osId;
    private final String numeroOs;
    private StatusExecucao status;
    private final Instant entradaNaFila;
    private Instant inicioExecucao;
    private Instant fimExecucao;

    public ItemFilaExecucao(UUID osId, String numeroOs) {
        this.osId = osId;
        this.numeroOs = numeroOs;
        this.status = StatusExecucao.NA_FILA;
        this.entradaNaFila = Instant.now();
    }

    private ItemFilaExecucao(UUID osId, String numeroOs, StatusExecucao status,
                             Instant entrada, Instant inicio, Instant fim) {
        this.osId = osId; this.numeroOs = numeroOs; this.status = status;
        this.entradaNaFila = entrada; this.inicioExecucao = inicio; this.fimExecucao = fim;
    }

    public static ItemFilaExecucao restaurar(UUID osId, String numeroOs, StatusExecucao status,
                                             Instant entrada, Instant inicio, Instant fim) {
        return new ItemFilaExecucao(osId, numeroOs, status, entrada, inicio, fim);
    }

    public void iniciar() {
        if (status != StatusExecucao.NA_FILA) throw new IllegalStateException("Só é possível iniciar item NA_FILA");
        this.status = StatusExecucao.EM_EXECUCAO;
        this.inicioExecucao = Instant.now();
    }

    public void finalizar() {
        if (status != StatusExecucao.EM_EXECUCAO) throw new IllegalStateException("Só é possível finalizar item EM_EXECUCAO");
        this.status = StatusExecucao.FINALIZADA;
        this.fimExecucao = Instant.now();
    }

    public void cancelar() { this.status = StatusExecucao.CANCELADA; }

    public UUID getOsId() { return osId; }
    public String getNumeroOs() { return numeroOs; }
    public StatusExecucao getStatus() { return status; }
    public Instant getEntradaNaFila() { return entradaNaFila; }
    public Instant getInicioExecucao() { return inicioExecucao; }
    public Instant getFimExecucao() { return fimExecucao; }
}

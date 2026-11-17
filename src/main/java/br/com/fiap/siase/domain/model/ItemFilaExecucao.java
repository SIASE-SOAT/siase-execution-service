package br.com.fiap.siase.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Item na fila de execução de serviços (persistido no DynamoDB). */
public class ItemFilaExecucao {
    private final UUID osId;
    private final String numeroOs;
    private final String clienteNome;
    private final String veiculo;
    private final List<String> itensServico = new ArrayList<>();
    private String mecanico;
    private StatusExecucao status;
    private final Instant entradaNaFila;
    private Instant inicioExecucao;
    private Instant fimExecucao;

    public ItemFilaExecucao(UUID osId, String numeroOs) {
        this(osId, numeroOs, null, null, List.of());
    }

    public ItemFilaExecucao(UUID osId, String numeroOs, String clienteNome,
                            String veiculo, List<String> itensServico) {
        this.osId = osId;
        this.numeroOs = numeroOs;
        this.clienteNome = clienteNome;
        this.veiculo = veiculo;
        if (itensServico != null) this.itensServico.addAll(itensServico);
        this.status = StatusExecucao.NA_FILA;
        this.entradaNaFila = Instant.now();
    }

    private ItemFilaExecucao(UUID osId, String numeroOs, String clienteNome, String veiculo,
                             List<String> itensServico, String mecanico, StatusExecucao status,
                             Instant entrada, Instant inicio, Instant fim) {
        this.osId = osId;
        this.numeroOs = numeroOs;
        this.clienteNome = clienteNome;
        this.veiculo = veiculo;
        if (itensServico != null) this.itensServico.addAll(itensServico);
        this.mecanico = mecanico;
        this.status = status;
        this.entradaNaFila = entrada;
        this.inicioExecucao = inicio;
        this.fimExecucao = fim;
    }

    public static ItemFilaExecucao restaurar(UUID osId, String numeroOs, StatusExecucao status,
                                             Instant entrada, Instant inicio, Instant fim) {
        return restaurar(osId, numeroOs, null, null, List.of(), null, status, entrada, inicio, fim);
    }

    public static ItemFilaExecucao restaurar(UUID osId, String numeroOs, String clienteNome,
                                             String veiculo, List<String> itensServico, String mecanico,
                                             StatusExecucao status, Instant entrada, Instant inicio,
                                             Instant fim) {
        return new ItemFilaExecucao(osId, numeroOs, clienteNome, veiculo, itensServico,
                mecanico, status, entrada, inicio, fim);
    }

    public void iniciar() {
        iniciar(null);
    }

    public void iniciar(String mecanico) {
        if (status != StatusExecucao.NA_FILA) throw new IllegalStateException("Só é possível iniciar item NA_FILA");
        this.mecanico = mecanico;
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
    public String getClienteNome() { return clienteNome; }
    public String getVeiculo() { return veiculo; }
    public List<String> getItensServico() { return List.copyOf(itensServico); }
    public String getMecanico() { return mecanico; }
    public StatusExecucao getStatus() { return status; }
    public Instant getEntradaNaFila() { return entradaNaFila; }
    public Instant getInicioExecucao() { return inicioExecucao; }
    public Instant getFimExecucao() { return fimExecucao; }
}

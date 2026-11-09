package br.com.fiap.siase.domain.enums;

public enum StatusExecucao {
    NA_FILA("Na Fila"),
    EM_EXECUCAO("Em Execução"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String descricao;
    StatusExecucao(String d) { this.descricao = d; }
    public String getDescricao() { return descricao; }
}

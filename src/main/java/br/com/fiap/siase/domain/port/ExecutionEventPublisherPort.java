package br.com.fiap.siase.domain.port;

public interface ExecutionEventPublisherPort {
    void publicarExecucaoIniciada(String osNumero, String osId);
    void publicarExecucaoFinalizada(String osNumero, String osId);
}

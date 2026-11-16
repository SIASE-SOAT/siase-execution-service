package br.com.fiap.siase.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Evento publicado na os-updates-queue — mesmo envelope do os-service (RFC-001). */
public record EventoExecucao(
        String tipoEvento,
        UUID osId,
        String numeroOs,
        String payload,
        Instant ocorridoEm
) {
    // produzidos
    public static final String EXECUCAO_INICIADA = "EXECUCAO_INICIADA";
    public static final String EXECUCAO_FINALIZADA = "EXECUCAO_FINALIZADA";
    public static final String EXECUCAO_CANCELADA = "EXECUCAO_CANCELADA";

    // consumidos (execution-queue, publicados pelo os-service)
    public static final String ORCAMENTO_APROVADO = "ORCAMENTO_APROVADO";
    public static final String OS_CANCELADA = "OS_CANCELADA";

    public static EventoExecucao de(String tipo, UUID osId, String numeroOs, String payload) {
        return new EventoExecucao(tipo, osId, numeroOs, payload, Instant.now());
    }
}

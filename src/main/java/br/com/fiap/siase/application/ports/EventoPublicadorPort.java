package br.com.fiap.siase.application.ports;


import br.com.fiap.siase.domain.model.EventoExecucao;

public interface EventoPublicadorPort { void publicar(EventoExecucao evento); }

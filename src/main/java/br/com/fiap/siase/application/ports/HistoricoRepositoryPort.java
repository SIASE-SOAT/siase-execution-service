package br.com.fiap.siase.application.ports;

import java.util.UUID;

public interface HistoricoRepositoryPort {
  void registrar(UUID osId, String evento, String detalhe);
}
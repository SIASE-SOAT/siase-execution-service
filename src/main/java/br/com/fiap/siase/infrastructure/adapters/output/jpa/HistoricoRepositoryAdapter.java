package br.com.fiap.siase.infrastructure.adapters.output.jpa;

import br.com.fiap.siase.application.ports.HistoricoRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Histórico de execução em Postgres (banco SQL do serviço — separado dos demais). */
@Component
public class HistoricoRepositoryAdapter implements HistoricoRepositoryPort {
  private final HistoricoJpaRepository repository;

  public HistoricoRepositoryAdapter(HistoricoJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public void registrar(UUID osId, String evento, String detalhe) {
    repository.save(new HistoricoJpaEntity(osId, evento, detalhe));
  }
}


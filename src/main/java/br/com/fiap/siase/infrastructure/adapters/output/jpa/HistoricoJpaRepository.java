package br.com.fiap.siase.infrastructure.adapters.output.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface HistoricoJpaRepository extends JpaRepository<HistoricoJpaEntity, Long> {
  List<HistoricoJpaEntity> findByOsIdOrderByRegistradoEmAsc(UUID osId);
}

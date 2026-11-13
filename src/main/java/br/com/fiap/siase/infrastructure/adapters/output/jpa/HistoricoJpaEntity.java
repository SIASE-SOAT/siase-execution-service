package br.com.fiap.siase.infrastructure.adapters.output.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "historico_execucao")
public class HistoricoJpaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "os_id", nullable = false)
  private UUID osId;
  @Column(nullable = false)
  private String evento;
  @Column(length = 2000)
  private String detalhe;
  @Column(name = "registrado_em", nullable = false)
  private Instant registradoEm = Instant.now();

  protected HistoricoJpaEntity() {}
  public HistoricoJpaEntity(UUID osId, String evento, String detalhe) {
    this.osId = osId; this.evento = evento; this.detalhe = detalhe;
  }
  public Long getId() { return id; }
}

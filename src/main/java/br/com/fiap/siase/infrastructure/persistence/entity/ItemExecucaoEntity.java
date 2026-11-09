package br.com.fiap.siase.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "itens_execucao")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemExecucaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "os_id", nullable = false)
    private String osId;

    @Column(name = "os_numero", nullable = false)
    private String osNumero;

    @Column(name = "servico_nome")
    private String servicoNome;

    @Column(name = "mecanico")
    private String mecanico;

    @Column(name = "iniciado_em")
    private LocalDateTime iniciadoEm;

    @Column(name = "finalizado_em")
    private LocalDateTime finalizadoEm;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;
}

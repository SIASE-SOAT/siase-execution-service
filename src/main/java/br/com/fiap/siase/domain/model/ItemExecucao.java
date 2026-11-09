package br.com.fiap.siase.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemExecucao {
    private UUID id;
    private String osId;
    private String osNumero;
    private String servicoNome;
    private String mecanico;
    private LocalDateTime iniciadoEm;
    private LocalDateTime finalizadoEm;
    private LocalDateTime criadoEm;
}

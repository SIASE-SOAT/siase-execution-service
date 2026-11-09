package br.com.fiap.siase.domain.model;

import br.com.fiap.siase.domain.enums.StatusExecucao;
import br.com.fiap.siase.domain.exception.BusinessException;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FilaExecucao {
    private String osId;
    private String osNumero;
    private String clienteNome;
    private String veiculoPlaca;
    @Builder.Default
    private StatusExecucao status = StatusExecucao.NA_FILA;
    @Builder.Default
    private List<String> itensServico = new ArrayList<>();
    private LocalDateTime enfileiradoEm;
    private LocalDateTime iniciadoEm;
    private LocalDateTime finalizadoEm;
    private LocalDateTime atualizadoEm;

    public void iniciar() {
        if (this.status != StatusExecucao.NA_FILA)
            throw new BusinessException("Apenas itens na fila podem ser iniciados. Status atual: " + this.status.getDescricao());
        this.status = StatusExecucao.EM_EXECUCAO;
        this.iniciadoEm = LocalDateTime.now();
    }

    public void finalizar() {
        if (this.status != StatusExecucao.EM_EXECUCAO)
            throw new BusinessException("Apenas execuções em andamento podem ser finalizadas. Status atual: " + this.status.getDescricao());
        this.status = StatusExecucao.FINALIZADA;
        this.finalizadoEm = LocalDateTime.now();
    }

    public void cancelar() {
        if (this.status == StatusExecucao.FINALIZADA)
            throw new BusinessException("Execução já finalizada não pode ser cancelada.");
        this.status = StatusExecucao.CANCELADA;
    }
}

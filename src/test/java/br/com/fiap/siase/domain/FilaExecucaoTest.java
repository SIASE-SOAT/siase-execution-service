package br.com.fiap.siase.domain;

import br.com.fiap.siase.domain.enums.StatusExecucao;
import br.com.fiap.siase.domain.exception.BusinessException;
import br.com.fiap.siase.domain.model.FilaExecucao;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FilaExecucaoTest {

    private FilaExecucao filaNoFila() {
        return FilaExecucao.builder()
                .osId("OS-ID-001").osNumero("OS-20250101-ABC")
                .status(StatusExecucao.NA_FILA).build();
    }

    @Test
    void deveIniciarExecucaoQuandoNaFila() {
        var fila = filaNoFila();
        fila.iniciar();
        assertThat(fila.getStatus()).isEqualTo(StatusExecucao.EM_EXECUCAO);
        assertThat(fila.getIniciadoEm()).isNotNull();
    }

    @Test
    void deveFinalizarExecucaoQuandoEmExecucao() {
        var fila = filaNoFila();
        fila.iniciar();
        fila.finalizar();
        assertThat(fila.getStatus()).isEqualTo(StatusExecucao.FINALIZADA);
        assertThat(fila.getFinalizadoEm()).isNotNull();
    }

    @Test
    void naoDeveIniciarExecucaoJaEmExecucao() {
        var fila = filaNoFila();
        fila.iniciar();
        assertThatThrownBy(fila::iniciar)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("na fila");
    }

    @Test
    void naoDeveFinalizarExecucaoNaFila() {
        var fila = filaNoFila();
        assertThatThrownBy(fila::finalizar)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("em andamento");
    }

    @Test
    void deveCancelarExecucaoNaFila() {
        var fila = filaNoFila();
        fila.cancelar();
        assertThat(fila.getStatus()).isEqualTo(StatusExecucao.CANCELADA);
    }

    @Test
    void naoDeveCancelarExecucaoFinalizada() {
        var fila = filaNoFila();
        fila.iniciar();
        fila.finalizar();
        assertThatThrownBy(fila::cancelar)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("finalizada");
    }
}

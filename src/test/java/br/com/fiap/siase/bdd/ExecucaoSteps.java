package br.com.fiap.siase.bdd;

import br.com.fiap.siase.application.usecase.*;
import br.com.fiap.siase.domain.enums.StatusExecucao;
import br.com.fiap.siase.domain.exception.BusinessException;
import br.com.fiap.siase.domain.model.FilaExecucao;
import br.com.fiap.siase.domain.model.ItemExecucao;
import br.com.fiap.siase.domain.port.*;
import io.cucumber.java.pt.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ExecucaoSteps {

    private final FilaExecucaoRepositoryPort filaRepo = mock(FilaExecucaoRepositoryPort.class);
    private final ItemExecucaoRepositoryPort itemRepo = mock(ItemExecucaoRepositoryPort.class);
    private final ExecutionEventPublisherPort pub = mock(ExecutionEventPublisherPort.class);

    private final EnfileirarOsUC enfileirarUC = new EnfileirarOsUC(filaRepo, pub);
    private final IniciarExecucaoUC iniciarUC = new IniciarExecucaoUC(filaRepo, itemRepo, pub);
    private final FinalizarExecucaoUC finalizarUC = new FinalizarExecucaoUC(filaRepo, itemRepo, pub);
    private final CancelarExecucaoUC cancelarUC = new CancelarExecucaoUC(filaRepo);

    private FilaExecucao filaAtual;
    private Exception ultimaExcecao;
    private int vezesSalvo;

    @Dado("que não existe OS na fila com id {string}")
    public void naoExisteNaFila(String osId) {
        when(filaRepo.findByOsId(osId)).thenReturn(Optional.empty());
        when(filaRepo.save(any())).thenAnswer(inv -> {
            filaAtual = inv.getArgument(0);
            return filaAtual;
        });
        when(itemRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Dado("que a OS {string} já está na fila")
    public void jaEstaNaFila(String osId) {
        filaAtual = FilaExecucao.builder().osId(osId).osNumero("OS-20250101-TEST02")
                .status(StatusExecucao.NA_FILA).build();
        when(filaRepo.findByOsId(osId)).thenReturn(Optional.of(filaAtual));
        vezesSalvo = 0;
        when(filaRepo.save(any())).thenAnswer(inv -> { vezesSalvo++; return inv.getArgument(0); });
    }

    @Dado("que a OS {string} está na fila com status {string}")
    public void osNaFilaComStatus(String osId, String status) {
        filaAtual = FilaExecucao.builder().osId(osId).osNumero("OS-20250101-" + osId.substring(osId.length() - 3))
                .status(StatusExecucao.valueOf(status)).build();
        when(filaRepo.findByOsId(osId)).thenReturn(Optional.of(filaAtual));
        when(filaRepo.save(any())).thenAnswer(inv -> { filaAtual = inv.getArgument(0); return filaAtual; });
        when(itemRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Dado("que a OS {string} está em execução com status {string}")
    public void osEmExecucao(String osId, String status) {
        filaAtual = FilaExecucao.builder().osId(osId).osNumero("OS-20250101-" + osId.substring(osId.length() - 3))
                .status(StatusExecucao.valueOf(status)).build();
        when(filaRepo.findByOsId(osId)).thenReturn(Optional.of(filaAtual));
        when(filaRepo.save(any())).thenAnswer(inv -> { filaAtual = inv.getArgument(0); return filaAtual; });
        when(itemRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Quando("o evento {string} for recebido para a OS {string}")
    public void eventoRecebido(String tipo, String osNumero) {
        if ("ORCAMENTO_APROVADO".equals(tipo))
            enfileirarUC.executar("OS-ID-001", osNumero, "João", "ABC-1234", List.of("Troca de óleo"));
        else if ("OS_CANCELADA".equals(tipo))
            cancelarUC.executar("OS-ID-005", "OS cancelada");
    }

    @Quando("o evento {string} for recebido novamente para a OS {string}")
    public void eventoRecebidoNovamente(String tipo, String osNumero) {
        enfileirarUC.executar("OS-ID-002", osNumero, "João", "ABC-1234", List.of());
    }

    @Quando("o mecânico {string} iniciar a execução da OS {string}")
    public void iniciarExecucao(String mecanico, String osId) {
        try {
            filaAtual = iniciarUC.executar(osId, mecanico);
        } catch (BusinessException e) {
            ultimaExcecao = e;
        }
    }

    @Quando("o mecânico {string} tentar iniciar a execução da OS {string}")
    public void tentarIniciarExecucao(String mecanico, String osId) {
        try {
            iniciarUC.executar(osId, mecanico);
        } catch (BusinessException e) {
            ultimaExcecao = e;
        }
    }

    @Quando("a execução da OS {string} for finalizada")
    public void finalizarExecucao(String osId) {
        try {
            filaAtual = finalizarUC.executar(osId);
        } catch (BusinessException e) {
            ultimaExcecao = e;
        }
    }

    @Quando("tentar finalizar a execução da OS {string}")
    public void tentarFinalizar(String osId) {
        try {
            finalizarUC.executar(osId);
        } catch (BusinessException e) {
            ultimaExcecao = e;
        }
    }

    @Então("a OS deve estar na fila com status {string}")
    @Então("a OS deve ter status de execução {string}")
    public void verificarStatus(String status) {
        assertThat(filaAtual).isNotNull();
        assertThat(filaAtual.getStatus().name()).isEqualTo(status);
    }

    @Então("a fila deve conter apenas uma entrada para a OS")
    public void verificarIdempotencia() {
        assertThat(vezesSalvo).isZero();
    }

    @E("o evento {string} deve ser publicado")
    public void verificarEvento(String tipo) {
        switch (tipo) {
            case "EXECUCAO_INICIADA" -> verify(pub, atLeastOnce()).publicarExecucaoIniciada(any(), any());
            case "EXECUCAO_FINALIZADA" -> verify(pub, atLeastOnce()).publicarExecucaoFinalizada(any(), any());
        }
    }

    @Então("deve retornar erro de negócio {string}")
    public void verificarErro(String mensagem) {
        assertThat(ultimaExcecao).isInstanceOf(BusinessException.class);
        assertThat(ultimaExcecao.getMessage()).contains(mensagem);
    }
}

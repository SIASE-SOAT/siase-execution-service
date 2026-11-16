package br.com.fiap.siase.bdd;

import br.com.fiap.siase.application.Fakes;
import br.com.fiap.siase.application.usecase.*;
import br.com.fiap.siase.domain.model.EventoExecucao;
import br.com.fiap.siase.domain.model.StatusExecucao;
import io.cucumber.java.pt.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ExecucaoSteps {

  private final Fakes.FilaMemoria fila = new Fakes.FilaMemoria();
  private final Fakes.PublicadorMemoria pub = new Fakes.PublicadorMemoria();
  private final Fakes.HistoricoMemoria hist = new Fakes.HistoricoMemoria();
  private final EnfileirarOsUseCase enfileirar = new EnfileirarOsUseCase(fila, hist);
  private final IniciarExecucaoUseCase iniciar = new IniciarExecucaoUseCase(fila, pub, hist);
  private final FinalizarExecucaoUseCase finalizar = new FinalizarExecucaoUseCase(fila, pub, hist);
  private final CompensarExecucaoUseCase compensar = new CompensarExecucaoUseCase(fila, pub, hist);

  private UUID osId;

  @Dado("que o orçamento da OS foi aprovado")
  public void orcamentoAprovado() {
    osId = UUID.randomUUID();
    enfileirar.executar(osId, "OS-1000");
  }

  @Entao("a OS entra na fila de execução")
  public void entraNaFila() {
    assertEquals(StatusExecucao.NA_FILA, fila.buscarPorOsId(osId).orElseThrow().getStatus());
  }

  @Quando("o mecânico inicia a execução")
  public void mecanicoInicia() { iniciar.executar(osId); }

  @E("o mecânico finaliza a execução")
  public void mecanicoFinaliza() { finalizar.executar(osId); }

  @Entao("o evento EXECUCAO_INICIADA é publicado para o os-service")
  public void eventoIniciada() { assertTrue(pub.publicou(EventoExecucao.EXECUCAO_INICIADA)); }

  @Entao("o evento EXECUCAO_FINALIZADA é publicado para o os-service")
  public void eventoFinalizada() { assertTrue(pub.publicou(EventoExecucao.EXECUCAO_FINALIZADA)); }

  @Quando("a OS é cancelada pelo cliente")
  public void osCancelada() { compensar.executar(osId); }

  @Entao("a execução é cancelada e o evento EXECUCAO_CANCELADA é publicado")
  public void execucaoCancelada() {
    assertEquals(StatusExecucao.CANCELADA, fila.buscarPorOsId(osId).orElseThrow().getStatus());
    assertTrue(pub.publicou(EventoExecucao.EXECUCAO_CANCELADA));
  }
}

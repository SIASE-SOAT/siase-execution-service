package br.com.fiap.siase.application;

import br.com.fiap.siase.application.usecase.*;
import br.com.fiap.siase.domain.model.EventoExecucao;
import br.com.fiap.siase.domain.model.StatusExecucao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExecucaoUseCasesTest {

  Fakes.FilaMemoria fila;
  Fakes.PublicadorMemoria pub;
  Fakes.HistoricoMemoria hist;
  EnfileirarOsUseCase enfileirar;
  IniciarExecucaoUseCase iniciar;
  FinalizarExecucaoUseCase finalizar;
  CompensarExecucaoUseCase compensar;
  ConsultarFilaUseCase consultar;

  @BeforeEach
  void setup() {
    fila = new Fakes.FilaMemoria();
    pub = new Fakes.PublicadorMemoria();
    hist = new Fakes.HistoricoMemoria();
    enfileirar = new EnfileirarOsUseCase(fila, hist);
    iniciar = new IniciarExecucaoUseCase(fila, pub, hist);
    finalizar = new FinalizarExecucaoUseCase(fila, pub, hist);
    compensar = new CompensarExecucaoUseCase(fila, pub, hist);
    consultar = new ConsultarFilaUseCase(fila);
  }

  @Test
  void enfileiraOsAposOrcamentoAprovado() {
    UUID osId = UUID.randomUUID();
    enfileirar.executar(osId, "OS-0001");
    assertEquals(StatusExecucao.NA_FILA, fila.buscarPorOsId(osId).orElseThrow().getStatus());
    assertEquals(1, consultar.naFila().size());
  }

  @Test
  void enfileirarEhIdempotente() {
    UUID osId = UUID.randomUUID();
    enfileirar.executar(osId, "OS-0001");
    enfileirar.executar(osId, "OS-0001");
    assertEquals(1, fila.listarTodas().size());
  }

  @Test
  void dadosDaOsEMecanicoFicamDisponiveisNaExecucao() {
    UUID osId = UUID.randomUUID();
    enfileirar.executar(osId, "OS-0001", "Maria Silva", "ABC-1234",
            List.of("Troca de óleo", "Alinhamento"));
    iniciar.executar(osId, "João Mecânico");

    var item = fila.buscarPorOsId(osId).orElseThrow();
    assertEquals("Maria Silva", item.getClienteNome());
    assertEquals("ABC-1234", item.getVeiculo());
    assertEquals(List.of("Troca de óleo", "Alinhamento"), item.getItensServico());
    assertEquals("João Mecânico", item.getMecanico());
    assertTrue(pub.eventos.get(0).payload().contains("João Mecânico"));
  }

  @Test
  void iniciarPublicaExecucaoIniciada() {
    UUID osId = UUID.randomUUID();
    enfileirar.executar(osId, "OS-0002");
    iniciar.executar(osId);
    assertEquals(StatusExecucao.EM_EXECUCAO, fila.buscarPorOsId(osId).orElseThrow().getStatus());
    assertTrue(pub.publicou(EventoExecucao.EXECUCAO_INICIADA));
  }

  @Test
  void iniciarSemEstarNaFilaFalha() {
    assertThrows(IllegalArgumentException.class, () -> iniciar.executar(UUID.randomUUID()));
  }

  @Test
  void finalizarPublicaExecucaoFinalizada() {
    UUID osId = UUID.randomUUID();
    enfileirar.executar(osId, "OS-0003");
    iniciar.executar(osId);
    finalizar.executar(osId);
    assertEquals(StatusExecucao.FINALIZADA, fila.buscarPorOsId(osId).orElseThrow().getStatus());
    assertTrue(pub.publicou(EventoExecucao.EXECUCAO_FINALIZADA));
  }

  @Test
  void finalizarSemIniciarFalha() {
    UUID osId = UUID.randomUUID();
    enfileirar.executar(osId, "OS-0004");
    assertThrows(IllegalStateException.class, () -> finalizar.executar(osId));
  }

  @Test
  void compensacaoCancelaItemNaFila() {
    UUID osId = UUID.randomUUID();
    enfileirar.executar(osId, "OS-0005");
    compensar.executar(osId);
    assertEquals(StatusExecucao.CANCELADA, fila.buscarPorOsId(osId).orElseThrow().getStatus());
    assertTrue(pub.publicou(EventoExecucao.EXECUCAO_CANCELADA));
  }

  @Test
  void compensacaoDeOsAusenteEhNoop() {
    UUID osId = UUID.randomUUID();
    assertDoesNotThrow(() -> compensar.executar(osId));
    assertFalse(pub.publicou(EventoExecucao.EXECUCAO_CANCELADA));
  }
}

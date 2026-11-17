package br.com.fiap.siase.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ItemFilaExecucaoTest {

  @Test
  void criaItemNaFilaComDadosDaOs() {
    UUID osId = UUID.randomUUID();

    ItemFilaExecucao item = new ItemFilaExecucao(
            osId,
            "OS-0001",
            "Maria Silva",
            "ABC-1234",
            List.of("Troca de óleo", "Alinhamento")
    );

    assertEquals(osId, item.getOsId());
    assertEquals("OS-0001", item.getNumeroOs());
    assertEquals("Maria Silva", item.getClienteNome());
    assertEquals("ABC-1234", item.getVeiculo());
    assertEquals(List.of("Troca de óleo", "Alinhamento"), item.getItensServico());
    assertEquals(StatusExecucao.NA_FILA, item.getStatus());
    assertNotNull(item.getEntradaNaFila());
    assertNull(item.getMecanico());
    assertNull(item.getInicioExecucao());
    assertNull(item.getFimExecucao());
  }

  @Test
  void construtorSimplesUsaListaVaziaEDadosOpcionaisNulos() {
    ItemFilaExecucao item = new ItemFilaExecucao(UUID.randomUUID(), "OS-0002");

    assertNull(item.getClienteNome());
    assertNull(item.getVeiculo());
    assertTrue(item.getItensServico().isEmpty());
    assertEquals(StatusExecucao.NA_FILA, item.getStatus());
  }

  @Test
  void aceitaListaDeServicosNula() {
    ItemFilaExecucao item = new ItemFilaExecucao(
            UUID.randomUUID(), "OS-0003", "Cliente", "XYZ-0000", null);

    assertTrue(item.getItensServico().isEmpty());
  }

  @Test
  void restauraItemComTodosOsCampos() {
    UUID osId = UUID.randomUUID();
    Instant entrada = Instant.parse("2026-11-16T10:00:00Z");
    Instant inicio = Instant.parse("2026-11-16T10:10:00Z");
    Instant fim = Instant.parse("2026-11-16T10:30:00Z");

    ItemFilaExecucao item = ItemFilaExecucao.restaurar(
            osId,
            "OS-0004",
            "Carlos",
            "DEF-5678",
            List.of("Revisão"),
            "João",
            StatusExecucao.FINALIZADA,
            entrada,
            inicio,
            fim
    );

    assertEquals(osId, item.getOsId());
    assertEquals("OS-0004", item.getNumeroOs());
    assertEquals("Carlos", item.getClienteNome());
    assertEquals("DEF-5678", item.getVeiculo());
    assertEquals(List.of("Revisão"), item.getItensServico());
    assertEquals("João", item.getMecanico());
    assertEquals(StatusExecucao.FINALIZADA, item.getStatus());
    assertEquals(entrada, item.getEntradaNaFila());
    assertEquals(inicio, item.getInicioExecucao());
    assertEquals(fim, item.getFimExecucao());
  }

  @Test
  void restauraItemComConstrutorSimples() {
    UUID osId = UUID.randomUUID();
    Instant entrada = Instant.parse("2026-11-16T10:00:00Z");

    ItemFilaExecucao item = ItemFilaExecucao.restaurar(
            osId, "OS-0005", StatusExecucao.NA_FILA, entrada, null, null);

    assertNull(item.getClienteNome());
    assertTrue(item.getItensServico().isEmpty());
    assertEquals(StatusExecucao.NA_FILA, item.getStatus());
    assertEquals(entrada, item.getEntradaNaFila());
  }

  @Test
  void iniciaExecucaoComMecanico() {
    ItemFilaExecucao item = new ItemFilaExecucao(UUID.randomUUID(), "OS-0006");

    item.iniciar("João Mecânico");

    assertEquals(StatusExecucao.EM_EXECUCAO, item.getStatus());
    assertEquals("João Mecânico", item.getMecanico());
    assertNotNull(item.getInicioExecucao());
  }

  @Test
  void iniciaExecucaoSemMecanico() {
    ItemFilaExecucao item = new ItemFilaExecucao(UUID.randomUUID(), "OS-0007");

    item.iniciar();

    assertEquals(StatusExecucao.EM_EXECUCAO, item.getStatus());
    assertNull(item.getMecanico());
    assertNotNull(item.getInicioExecucao());
  }

  @Test
  void naoIniciaItemForaDaFila() {
    ItemFilaExecucao item = ItemFilaExecucao.restaurar(
            UUID.randomUUID(), "OS-0008", StatusExecucao.CANCELADA,
            Instant.now(), null, null);

    IllegalStateException erro = assertThrows(IllegalStateException.class, item::iniciar);
    assertEquals("Só é possível iniciar item NA_FILA", erro.getMessage());
  }

  @Test
  void finalizaItemEmExecucao() {
    ItemFilaExecucao item = new ItemFilaExecucao(UUID.randomUUID(), "OS-0009");
    item.iniciar("João");

    item.finalizar();

    assertEquals(StatusExecucao.FINALIZADA, item.getStatus());
    assertNotNull(item.getFimExecucao());
  }

  @Test
  void naoFinalizaItemQueNaoEstaEmExecucao() {
    ItemFilaExecucao item = new ItemFilaExecucao(UUID.randomUUID(), "OS-0010");

    IllegalStateException erro = assertThrows(IllegalStateException.class, item::finalizar);
    assertEquals("Só é possível finalizar item EM_EXECUCAO", erro.getMessage());
  }

  @Test
  void cancelaItem() {
    ItemFilaExecucao item = new ItemFilaExecucao(UUID.randomUUID(), "OS-0011");

    item.cancelar();

    assertEquals(StatusExecucao.CANCELADA, item.getStatus());
  }

  @Test
  void listaDeServicosRetornadaEhImutavel() {
    ItemFilaExecucao item = new ItemFilaExecucao(
            UUID.randomUUID(), "OS-0012", "Cliente", "AAA-1111", List.of("Serviço"));

    assertThrows(UnsupportedOperationException.class,
            () -> item.getItensServico().add("Outro serviço"));
  }
}

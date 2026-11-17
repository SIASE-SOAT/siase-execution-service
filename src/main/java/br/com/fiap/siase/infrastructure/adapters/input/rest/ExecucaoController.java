package br.com.fiap.siase.infrastructure.adapters.input.rest;


import br.com.fiap.siase.application.usecase.ConsultarFilaUseCase;
import br.com.fiap.siase.application.usecase.FinalizarExecucaoUseCase;
import br.com.fiap.siase.application.usecase.IniciarExecucaoUseCase;
import br.com.fiap.siase.domain.model.ItemFilaExecucao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Execução", description = "Fila de execução de serviços — mecânicos")
@RestController
@RequestMapping("/api/execucao")
public class ExecucaoController {

  private final ConsultarFilaUseCase consultar;
  private final IniciarExecucaoUseCase iniciar;
  private final FinalizarExecucaoUseCase finalizar;

  public ExecucaoController(ConsultarFilaUseCase consultar,
                            IniciarExecucaoUseCase iniciar,
                            FinalizarExecucaoUseCase finalizar) {
    this.consultar = consultar;
    this.iniciar = iniciar;
    this.finalizar = finalizar;
  }

  @Operation(summary = "Fila de execução (itens aguardando)")
  @GetMapping("/fila")
  public List<ItemFilaExecucao> fila() { return consultar.naFila(); }

  @Operation(summary = "Todos os itens (fila + em execução + histórico)")
  @GetMapping
  public List<ItemFilaExecucao> todas() { return consultar.todas(); }

  @Operation(summary = "Iniciar execução de uma OS, opcionalmente informando o mecânico")
  @PostMapping("/{osId}/iniciar")
  public ResponseEntity<Void> iniciar(@PathVariable UUID osId,
                                      @RequestParam(required = false) String mecanico) {
    iniciar.executar(osId, mecanico);
    return ResponseEntity.accepted().build();
  }

  @Operation(summary = "Finalizar execução de uma OS")
  @PostMapping("/{osId}/finalizar")
  public ResponseEntity<Void> finalizar(@PathVariable UUID osId) {
    finalizar.executar(osId);
    return ResponseEntity.accepted().build();
  }
}

package br.com.fiap.siase.infrastructure.web;

import br.com.fiap.siase.application.usecase.FinalizarExecucaoUC;
import br.com.fiap.siase.application.usecase.IniciarExecucaoUC;
import br.com.fiap.siase.domain.model.FilaExecucao;
import br.com.fiap.siase.domain.port.FilaExecucaoRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/execucao")
@Tag(name = "Execução")
@RequiredArgsConstructor
public class ExecucaoController {

    private final IniciarExecucaoUC iniciarUC;
    private final FinalizarExecucaoUC finalizarUC;
    private final FilaExecucaoRepositoryPort filaRepository;

    @GetMapping("/fila")
    @Operation(summary = "Listar fila de execução")
    public ResponseEntity<List<FilaExecucao>> listarFila() {
        return ResponseEntity.ok(filaRepository.findAll());
    }

    @GetMapping("/fila/{osId}")
    @Operation(summary = "Consultar OS na fila")
    public ResponseEntity<FilaExecucao> consultarFila(@PathVariable String osId) {
        return filaRepository.findByOsId(osId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/fila/{osId}/iniciar")
    @Operation(summary = "Iniciar execução da OS")
    public ResponseEntity<FilaExecucao> iniciar(@PathVariable String osId,
                                                 @RequestParam(defaultValue = "mecanico") String mecanico) {
        return ResponseEntity.ok(iniciarUC.executar(osId, mecanico));
    }

    @PatchMapping("/fila/{osId}/finalizar")
    @Operation(summary = "Finalizar execução da OS")
    public ResponseEntity<FilaExecucao> finalizar(@PathVariable String osId) {
        return ResponseEntity.ok(finalizarUC.executar(osId));
    }
}

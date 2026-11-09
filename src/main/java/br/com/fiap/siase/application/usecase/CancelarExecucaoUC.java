package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.domain.model.FilaExecucao;
import br.com.fiap.siase.domain.port.FilaExecucaoRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CancelarExecucaoUC {

    private final FilaExecucaoRepositoryPort filaRepository;

    public void executar(String osId, String motivo) {
        filaRepository.findByOsId(osId).ifPresent(fila -> {
            try {
                fila.cancelar();
                filaRepository.save(fila);
                log.info("Execução cancelada para OS {} — motivo: {}", fila.getOsNumero(), motivo);
            } catch (Exception e) {
                log.warn("Não foi possível cancelar execução para OS {}: {}", osId, e.getMessage());
            }
        });
    }
}

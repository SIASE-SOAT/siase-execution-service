package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.domain.model.FilaExecucao;
import br.com.fiap.siase.domain.port.ExecutionEventPublisherPort;
import br.com.fiap.siase.domain.port.FilaExecucaoRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class EnfileirarOsUC {

    private final FilaExecucaoRepositoryPort filaRepository;
    private final ExecutionEventPublisherPort eventPublisher;

    public FilaExecucao executar(String osId, String osNumero, String clienteNome,
                                  String veiculoPlaca, List<String> itensServico) {
        // idempotência
        var existente = filaRepository.findByOsId(osId);
        if (existente.isPresent()) {
            log.info("OS {} já está na fila, ignorando duplicata", osNumero);
            return existente.get();
        }

        var fila = FilaExecucao.builder()
                .osId(osId).osNumero(osNumero)
                .clienteNome(clienteNome).veiculoPlaca(veiculoPlaca)
                .itensServico(itensServico != null ? itensServico : List.of())
                .enfileiradoEm(LocalDateTime.now())
                .build();

        var salvo = filaRepository.save(fila);
        log.info("OS {} enfileirada para execução", osNumero);
        return salvo;
    }
}

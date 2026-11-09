package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.domain.exception.ResourceNotFoundException;
import br.com.fiap.siase.domain.model.FilaExecucao;
import br.com.fiap.siase.domain.model.ItemExecucao;
import br.com.fiap.siase.domain.port.ExecutionEventPublisherPort;
import br.com.fiap.siase.domain.port.FilaExecucaoRepositoryPort;
import br.com.fiap.siase.domain.port.ItemExecucaoRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class IniciarExecucaoUC {

    private final FilaExecucaoRepositoryPort filaRepository;
    private final ItemExecucaoRepositoryPort itemRepository;
    private final ExecutionEventPublisherPort eventPublisher;

    public FilaExecucao executar(String osId, String mecanico) {
        var fila = filaRepository.findByOsId(osId)
                .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada na fila: " + osId));

        fila.iniciar();
        var salvo = filaRepository.save(fila);

        itemRepository.save(ItemExecucao.builder()
                .osId(osId).osNumero(fila.getOsNumero())
                .servicoNome("Execução iniciada").mecanico(mecanico)
                .iniciadoEm(LocalDateTime.now()).build());

        eventPublisher.publicarExecucaoIniciada(fila.getOsNumero(), osId);
        log.info("Execução iniciada para OS {} por mecânico {}", fila.getOsNumero(), mecanico);
        return salvo;
    }
}

package br.com.fiap.siase.domain.port;

import br.com.fiap.siase.domain.model.FilaExecucao;
import java.util.List;
import java.util.Optional;

public interface FilaExecucaoRepositoryPort {
    FilaExecucao save(FilaExecucao fila);
    Optional<FilaExecucao> findByOsId(String osId);
    List<FilaExecucao> findAll();
    void deleteByOsId(String osId);
}

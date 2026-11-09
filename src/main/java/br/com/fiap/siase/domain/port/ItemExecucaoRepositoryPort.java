package br.com.fiap.siase.domain.port;

import br.com.fiap.siase.domain.model.ItemExecucao;
import java.util.List;

public interface ItemExecucaoRepositoryPort {
    ItemExecucao save(ItemExecucao item);
    List<ItemExecucao> findByOsId(String osId);
}

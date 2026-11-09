package br.com.fiap.siase.infrastructure.persistence;

import br.com.fiap.siase.domain.model.ItemExecucao;
import br.com.fiap.siase.domain.port.ItemExecucaoRepositoryPort;
import br.com.fiap.siase.infrastructure.persistence.mapper.ItemExecucaoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ItemExecucaoRepositoryAdapter implements ItemExecucaoRepositoryPort {
    private final ItemExecucaoJpaRepository jpa;
    private final ItemExecucaoMapper mapper;

    @Override
    public ItemExecucao save(ItemExecucao item) { return mapper.toDomain(jpa.save(mapper.toEntity(item))); }

    @Override
    public List<ItemExecucao> findByOsId(String osId) {
        return jpa.findByOsId(osId).stream().map(mapper::toDomain).toList();
    }
}

package br.com.fiap.siase.infrastructure.persistence;

import br.com.fiap.siase.infrastructure.persistence.entity.ItemExecucaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ItemExecucaoJpaRepository extends JpaRepository<ItemExecucaoEntity, UUID> {
    List<ItemExecucaoEntity> findByOsId(String osId);
}

package br.com.fiap.siase.infrastructure.persistence.mapper;

import br.com.fiap.siase.domain.model.ItemExecucao;
import br.com.fiap.siase.infrastructure.persistence.entity.ItemExecucaoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemExecucaoMapper {
    ItemExecucao toDomain(ItemExecucaoEntity e);
    ItemExecucaoEntity toEntity(ItemExecucao d);
}

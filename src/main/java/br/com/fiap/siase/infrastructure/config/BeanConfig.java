package br.com.fiap.siase.infrastructure.config;

import br.com.fiap.siase.application.usecase.*;
import br.com.fiap.siase.domain.port.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public EnfileirarOsUC enfileirarOsUC(FilaExecucaoRepositoryPort fila, ExecutionEventPublisherPort pub) {
        return new EnfileirarOsUC(fila, pub);
    }

    @Bean
    public IniciarExecucaoUC iniciarExecucaoUC(FilaExecucaoRepositoryPort fila,
                                                ItemExecucaoRepositoryPort item,
                                                ExecutionEventPublisherPort pub) {
        return new IniciarExecucaoUC(fila, item, pub);
    }

    @Bean
    public FinalizarExecucaoUC finalizarExecucaoUC(FilaExecucaoRepositoryPort fila,
                                                    ItemExecucaoRepositoryPort item,
                                                    ExecutionEventPublisherPort pub) {
        return new FinalizarExecucaoUC(fila, item, pub);
    }

    @Bean
    public CancelarExecucaoUC cancelarExecucaoUC(FilaExecucaoRepositoryPort fila) {
        return new CancelarExecucaoUC(fila);
    }
}

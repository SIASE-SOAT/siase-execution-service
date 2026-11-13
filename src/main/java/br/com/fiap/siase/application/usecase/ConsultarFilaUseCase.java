package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.ports.FilaExecucaoRepositoryPort;
import br.com.fiap.siase.domain.model.ItemFilaExecucao;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ConsultarFilaUseCase {
    private final FilaExecucaoRepositoryPort fila;
    public ConsultarFilaUseCase(FilaExecucaoRepositoryPort fila) { this.fila = fila; }
    public List<ItemFilaExecucao> naFila() { return fila.listarNaFila(); }
    public List<ItemFilaExecucao> todas() { return fila.listarTodas(); }
}

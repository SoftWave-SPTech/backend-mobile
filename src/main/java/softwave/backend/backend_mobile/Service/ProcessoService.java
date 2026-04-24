package softwave.backend.backend_mobile.Service;

import org.springframework.stereotype.Service;
import softwave.backend.backend_mobile.DTO.ProcessoRequestDTO;
import softwave.backend.backend_mobile.DTO.ProcessoResponseDTO;
import softwave.backend.backend_mobile.Entity.ProcessoEntity;
import softwave.backend.backend_mobile.Exception.NotFoundException;
import softwave.backend.backend_mobile.Repository.ClienteRepository;
import softwave.backend.backend_mobile.Repository.ProcessoRepository;

import java.util.List;

@Service
public class ProcessoService {

    private final ProcessoRepository repository;
    private final ClienteRepository clienteRepository;

    public ProcessoService(ProcessoRepository repository, ClienteRepository clienteRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
    }

    // 🔹 LISTAR

    public List<ProcessoResponseDTO> listar() {
        return repository.findAll()
                .stream()
                .map(ProcessoResponseDTO::new)
                .toList();
    }

    // 🔹 BUSCAR POR ID

    public ProcessoResponseDTO buscarPorId(Integer id) {
        ProcessoEntity processo = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Processo não encontrado"));

        return new ProcessoResponseDTO(processo);
    }

    // 🔹 CRIAR

    public ProcessoResponseDTO criar(ProcessoRequestDTO dto) {

        ProcessoEntity processo = new ProcessoEntity();

        processo.setNumeroProcesso(dto.getNumeroProcesso());
        processo.setTitulo(dto.getTitulo());
        processo.setDescricao(dto.getDescricao());
        processo.setStatus(dto.getStatus());
        processo.setDataInicio(dto.getDataInicio());
        processo.setDataFim(dto.getDataFim());
        processo.setCategoria(dto.getCategoria());

        if (dto.getClienteId() != null) {
            var cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
            processo.setCliente(cliente);
        }

        return new ProcessoResponseDTO(repository.save(processo));
    }

    // 🔹 ATUALIZAR

    public ProcessoResponseDTO atualizar(Integer id, ProcessoRequestDTO dto) {

        ProcessoEntity processo = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Processo não encontrado"));

        processo.setNumeroProcesso(dto.getNumeroProcesso());
        processo.setTitulo(dto.getTitulo());
        processo.setDescricao(dto.getDescricao());
        processo.setStatus(dto.getStatus());
        processo.setDataInicio(dto.getDataInicio());
        processo.setDataFim(dto.getDataFim());
        processo.setCategoria(dto.getCategoria());

        if (dto.getClienteId() != null) {
            var cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
            processo.setCliente(cliente);
        }

        return new ProcessoResponseDTO(repository.save(processo));
    }

    // 🔹 DELETAR

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    // 🔹 BUSCAS

    public ProcessoResponseDTO buscarPorNumero(String numero) {
        return repository.findByNumeroProcesso(numero)
                .map(ProcessoResponseDTO::new)
                .orElseThrow(() -> new NotFoundException("Processo não encontrado"));
    }

    public List<ProcessoResponseDTO> buscarPorTitulo(String titulo) {
        return repository.findByTituloContainingIgnoreCase(titulo)
                .stream()
                .map(ProcessoResponseDTO::new)
                .toList();
    }

    public List<ProcessoResponseDTO> buscarPorCliente(Integer clienteId) {
        return repository.findByClienteId(clienteId)
                .stream()
                .map(ProcessoResponseDTO::new)
                .toList();
    }
}

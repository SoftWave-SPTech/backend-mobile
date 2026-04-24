package softwave.backend.backend_mobile.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import softwave.backend.backend_mobile.Config.GoogleDriveService;
import softwave.backend.backend_mobile.DTO.ComprovanteRequestDTO;
import softwave.backend.backend_mobile.DTO.ComprovanteResponseDTO;
import softwave.backend.backend_mobile.Entity.ComprovanteEntity;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Repository.ComprovanteRepository;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ComprovanteService {

    private final ComprovanteRepository repository;
    private final TransacaoRepository transacaoRepository;
    private final GoogleDriveService driveService;

    public ComprovanteService(ComprovanteRepository repository,
                              TransacaoRepository transacaoRepository, GoogleDriveService driveService) {
        this.repository = repository;
        this.transacaoRepository = transacaoRepository;
        this.driveService = driveService;
    }

    // 🔹 CRUD

    public List<ComprovanteResponseDTO> listar() {
        return repository.findAll()
                .stream()
                .map(ComprovanteResponseDTO::new)
                .toList();
    }

    public ComprovanteResponseDTO buscarPorId(Integer id) {
        return new ComprovanteResponseDTO(buscarEntity(id));
    }

    public ComprovanteResponseDTO criar(ComprovanteRequestDTO dto) {

        ComprovanteEntity c = new ComprovanteEntity();

        c.setNomeArquivo(dto.getNomeArquivo());
        c.setCaminhoArquivo(dto.getCaminhoArquivo());
        c.setDataUpload(LocalDate.now());

        if (dto.getTransacaoId() != null) {
            var transacao = transacaoRepository.findById(dto.getTransacaoId())
                    .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

            c.setTransacao(transacao);
        }

        return new ComprovanteResponseDTO(repository.save(c));
    }

    public ComprovanteResponseDTO atualizar(Integer id, ComprovanteRequestDTO dto) {

        ComprovanteEntity c = buscarEntity(id);

        c.setNomeArquivo(dto.getNomeArquivo());
        c.setCaminhoArquivo(dto.getCaminhoArquivo());

        return new ComprovanteResponseDTO(repository.save(c));
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    // 🔹 REGRAS

    // ✅ anexar comprovante
    public ComprovanteResponseDTO anexarArquivo(Integer transacaoId, MultipartFile file) throws Exception {

        TransacaoEntity t = transacaoRepository.findById(transacaoId)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

        String fileId = driveService.uploadArquivo(file, transacaoId);

        String url = "https://drive.google.com/file/d/" + fileId;

        ComprovanteEntity c = new ComprovanteEntity();

        c.setNomeArquivo(file.getOriginalFilename()); // nome original
        c.setCaminhoArquivo(url);
        c.setDriveFileId(fileId); // 🔥 ESSENCIAL
        c.setDataUpload(LocalDate.now());
        c.setTransacao(t);

        return new ComprovanteResponseDTO(repository.save(c));
    }

    // ✅ excluir comprovante
    public void excluirComprovante(Integer id) throws Exception {

        ComprovanteEntity c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comprovante não encontrado"));

        // 🔥 deleta do Drive
        if (c.getDriveFileId() != null) {
            driveService.deletarArquivo(c.getDriveFileId());
        }

        // 🔥 deleta do banco
        repository.deleteById(id);
    }

    // 🔹 buscar por transação
    public List<ComprovanteResponseDTO> buscarPorTransacao(Integer transacaoId) {

        return repository.findByTransacaoId(transacaoId)
                .stream()
                .map(ComprovanteResponseDTO::new)
                .toList();
    }

    // 🔹 helper
    private ComprovanteEntity buscarEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comprovante não encontrado"));
    }
}

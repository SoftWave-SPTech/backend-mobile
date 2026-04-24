package softwave.backend.backend_mobile.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import softwave.backend.backend_mobile.DTO.ComprovanteRequestDTO;
import softwave.backend.backend_mobile.DTO.ComprovanteResponseDTO;
import softwave.backend.backend_mobile.Service.ComprovanteService;

import java.util.List;

@RestController
@RequestMapping("/comprovantes")
public class ComprovanteController {

    private final ComprovanteService service;

    public ComprovanteController(ComprovanteService service) {
        this.service = service;
    }

    // 🔹 CRUD

    @GetMapping
    public List<ComprovanteResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ComprovanteResponseDTO buscar(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ComprovanteResponseDTO criar(@RequestBody ComprovanteRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public ComprovanteResponseDTO atualizar(@PathVariable Integer id,
                                            @RequestBody ComprovanteRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        service.deletar(id);
    }

    // 🔹 REGRAS

    @PostMapping("/upload/{transacaoId}")
    public ComprovanteResponseDTO upload(
            @PathVariable Integer transacaoId,
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        return service.anexarArquivo(transacaoId, file);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Integer id) throws Exception {
        service.excluirComprovante(id);
    }

    @GetMapping("/transacao/{transacaoId}")
    public List<ComprovanteResponseDTO> buscarPorTransacao(@PathVariable Integer transacaoId) {
        return service.buscarPorTransacao(transacaoId);
    }
}

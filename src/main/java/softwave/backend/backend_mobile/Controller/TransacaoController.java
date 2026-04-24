package softwave.backend.backend_mobile.Controller;

import org.springframework.web.bind.annotation.*;
import softwave.backend.backend_mobile.DTO.TransacaoRequestDTO;
import softwave.backend.backend_mobile.DTO.TransacaoResponseDTO;
import softwave.backend.backend_mobile.Service.TransacaoService;

import java.util.List;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService service;

    public TransacaoController(TransacaoService service) {
        this.service = service;
    }

    // 🔹 CRUD

    @GetMapping
    public List<TransacaoResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public TransacaoResponseDTO buscar(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public TransacaoResponseDTO criar(@RequestBody TransacaoRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public TransacaoResponseDTO atualizar(@PathVariable Integer id,
                                          @RequestBody TransacaoRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        service.deletar(id);
    }

    // 🔹 REGRAS

    @PostMapping("/{id}/gerar-cobranca")
    public TransacaoResponseDTO gerarCobranca(@PathVariable Integer id) {
        return service.gerarCobranca(id);
    }

    @PostMapping("/{id}/aprovar")
    public TransacaoResponseDTO aprovar(@PathVariable Integer id) {
        return service.aprovar(id);
    }

    @PostMapping("/{id}/reprovar")
    public TransacaoResponseDTO reprovar(@PathVariable Integer id) {
        return service.reprovar(id);
    }

    @PostMapping("/{id}/pagar")
    public TransacaoResponseDTO pagar(@PathVariable Integer id) {
        return service.marcarComoPago(id);
    }
}

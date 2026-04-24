package softwave.backend.backend_mobile.Controller;

import org.springframework.web.bind.annotation.*;
import softwave.backend.backend_mobile.DTO.ProcessoRequestDTO;
import softwave.backend.backend_mobile.DTO.ProcessoResponseDTO;
import softwave.backend.backend_mobile.Service.ProcessoService;

import java.util.List;

@RestController
@RequestMapping("/processos")
public class ProcessoController {

    private final ProcessoService service;

    public ProcessoController(ProcessoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProcessoResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ProcessoResponseDTO buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ProcessoResponseDTO criar(@RequestBody ProcessoRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public ProcessoResponseDTO atualizar(@PathVariable Integer id,
                                         @RequestBody ProcessoRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        service.deletar(id);
    }

    // 🔹 BUSCAS

    @GetMapping("/numero/{numero}")
    public ProcessoResponseDTO buscarPorNumero(@PathVariable String numero) {
        return service.buscarPorNumero(numero);
    }

    @GetMapping("/titulo")
    public List<ProcessoResponseDTO> buscarPorTitulo(@RequestParam String titulo) {
        return service.buscarPorTitulo(titulo);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<ProcessoResponseDTO> buscarPorCliente(@PathVariable Integer clienteId) {
        return service.buscarPorCliente(clienteId);
    }
}

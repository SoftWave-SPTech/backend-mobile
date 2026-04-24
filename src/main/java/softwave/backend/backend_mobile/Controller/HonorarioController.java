package softwave.backend.backend_mobile.Controller;

import org.springframework.web.bind.annotation.*;
import softwave.backend.backend_mobile.DTO.HonorarioRequestDTO;
import softwave.backend.backend_mobile.DTO.HonorarioResponseDTO;
import softwave.backend.backend_mobile.DTO.TransacaoResponseDTO;
import softwave.backend.backend_mobile.Service.HonorarioService;

import java.util.List;

@RestController
@RequestMapping("/honorarios")
public class HonorarioController {

    private final HonorarioService service;

    public HonorarioController(HonorarioService service) {
        this.service = service;
    }

    // 🔹 CRUD

    @GetMapping
    public List<HonorarioResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public HonorarioResponseDTO buscar(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public HonorarioResponseDTO criar(@RequestBody HonorarioRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public HonorarioResponseDTO atualizar(@PathVariable Integer id,
                                          @RequestBody HonorarioRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        service.deletar(id);
    }

    // 🔹 REGRAS

    @GetMapping("/{id}/valor-pago")
    public Double valorPago(@PathVariable Integer id) {
        return service.calcularValorPago(id);
    }

    @GetMapping("/{id}/valor-pendente")
    public Double valorPendente(@PathVariable Integer id) {
        return service.calcularValorPendente(id);
    }

    @PostMapping("/{id}/gerar-parcelas")
    public List<TransacaoResponseDTO> gerarParcelas(@PathVariable Integer id,
                                                    @RequestParam Integer quantidade) {
        return service.gerarParcelas(id, quantidade);
    }


    @GetMapping("/processo/{processoId}")
    public List<HonorarioResponseDTO> buscarPorProcesso(@PathVariable Integer processoId) {
        return service.buscarPorProcesso(processoId);
    }
}

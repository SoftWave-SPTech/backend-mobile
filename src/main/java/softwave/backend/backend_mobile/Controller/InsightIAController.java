package softwave.backend.backend_mobile.Controller;

import org.springframework.web.bind.annotation.*;
import softwave.backend.backend_mobile.DTO.InsightIARequestDTO;
import softwave.backend.backend_mobile.DTO.InsightIAResponseDTO;
import softwave.backend.backend_mobile.Service.InsightIAService;

import java.util.List;

@RestController
@RequestMapping("/insights-ia")
public class InsightIAController {

    private final InsightIAService service;

    public InsightIAController(InsightIAService service) {
        this.service = service;
    }

    // 🔹 CRUD

    @GetMapping
    public List<InsightIAResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public InsightIAResponseDTO buscar(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public InsightIAResponseDTO criar(@RequestBody InsightIARequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public InsightIAResponseDTO atualizar(@PathVariable Integer id,
                                          @RequestBody InsightIARequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        service.deletar(id);
    }

    // 🔥 GERAR INSIGHT

    @PostMapping("/gerar")
    public InsightIAResponseDTO gerar(@RequestBody InsightIARequestDTO dto) {
        return service.gerarInsight(dto);
    }
}

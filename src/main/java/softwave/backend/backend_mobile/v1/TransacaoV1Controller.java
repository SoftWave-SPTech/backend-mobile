package softwave.backend.backend_mobile.v1;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import softwave.backend.backend_mobile.service.V1TransacaoService;
import softwave.backend.backend_mobile.v1.dto.TransacaoCreateRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/transacoes")
public class TransacaoV1Controller {

    private final V1TransacaoService transacaoService;

    public TransacaoV1Controller(V1TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public List<Map<String, Object>> listar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int limit
    ) {
        return transacaoService.listar(jwt, tipo, status, busca, dataInicio, dataFim, page, limit);
    }

    @GetMapping("/{id}")
    public Map<String, Object> detalhe(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        return transacaoService.detalhe(jwt, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> criar(@AuthenticationPrincipal Jwt jwt, @RequestBody TransacaoCreateRequest body) {
        return transacaoService.criar(jwt, body);
    }

    @PatchMapping("/{id}")
    public Map<String, Object> patch(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @RequestBody Map<String, Object> campos
    ) {
        return transacaoService.atualizarParcial(jwt, id, campos);
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> status(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @RequestBody Map<String, String> body
    ) {
        return transacaoService.atualizarStatus(jwt, id, body.get("status"));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        return transacaoService.excluir(jwt, id);
    }

    @PostMapping("/{id}/comprovante")
    public Map<String, Object> comprovante(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @RequestPart("arquivo") MultipartFile arquivo
    ) throws Exception {
        return transacaoService.comprovante(jwt, id, arquivo);
    }
}

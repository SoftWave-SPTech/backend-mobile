package softwave.backend.backend_mobile.v1;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import softwave.backend.backend_mobile.service.V1ContratoService;

import java.util.Map;

@RestController
@RequestMapping("/v1/contratos")
public class ContratoV1Controller {

    private final V1ContratoService contratoService;

    public ContratoV1Controller(V1ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @GetMapping
    public Map<String, Object> listar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer clienteId
    ) {
        return contratoService.listar(jwt, status, clienteId);
    }

    @GetMapping("/{id}")
    public Map<String, Object> detalhe(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        return contratoService.detalhe(jwt, id);
    }

    @GetMapping("/{id}/parcelas")
    public Map<String, Object> parcelas(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        return contratoService.parcelas(jwt, id);
    }
}

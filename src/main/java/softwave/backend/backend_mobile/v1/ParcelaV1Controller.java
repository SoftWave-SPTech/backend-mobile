package softwave.backend.backend_mobile.v1;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import softwave.backend.backend_mobile.service.V1ContratoService;

import java.util.Map;

@RestController
@RequestMapping("/v1/parcelas")
public class ParcelaV1Controller {

    private final V1ContratoService contratoService;

    public ParcelaV1Controller(V1ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @PatchMapping("/{id}")
    public Map<String, Object> atualizar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @RequestBody Map<String, String> body
    ) {
        return contratoService.atualizarParcela(jwt, id, body.get("status"));
    }

    @PostMapping("/{id}/gerar-cobranca")
    public Map<String, Object> gerarCobranca(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        return contratoService.gerarCobranca(jwt, id);
    }
}

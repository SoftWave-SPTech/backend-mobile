package softwave.backend.backend_mobile.v1;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softwave.backend.backend_mobile.service.V1PagamentoService;

import java.util.Map;

@RestController
@RequestMapping("/v1/pagamentos")
public class PagamentoV1Controller {

    private final V1PagamentoService pagamentoService;

    public PagamentoV1Controller(V1PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/pendentes")
    public Map<String, Object> pendentes(@AuthenticationPrincipal Jwt jwt) {
        return pagamentoService.pendentes(jwt);
    }

    @PutMapping("/{id}/aprovar")
    public Map<String, Object> aprovar(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        return pagamentoService.aprovar(jwt, id);
    }

    @PutMapping("/{id}/reprovar")
    public Map<String, Object> reprovar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @RequestBody Map<String, String> body
    ) {
        return pagamentoService.reprovar(jwt, id, body.getOrDefault("motivo", ""));
    }

    @GetMapping("/{id}/comprovante")
    public ResponseEntity<Resource> comprovante(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id
    ) throws Exception {
        return pagamentoService.comprovante(jwt, id);
    }
}

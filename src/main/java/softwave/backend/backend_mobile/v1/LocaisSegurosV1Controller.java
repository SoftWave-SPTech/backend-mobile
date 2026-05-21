package softwave.backend.backend_mobile.v1;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import softwave.backend.backend_mobile.Service.V1LocaisSegurosService;

import java.util.Map;

@RestController
@RequestMapping("/v1/locais-seguros")
public class LocaisSegurosV1Controller {

    private final V1LocaisSegurosService locaisSegurosService;

    public LocaisSegurosV1Controller(V1LocaisSegurosService locaisSegurosService) {
        this.locaisSegurosService = locaisSegurosService;
    }

    @GetMapping
    public Map<String, Object> listar(@AuthenticationPrincipal Jwt jwt) {
        return locaisSegurosService.listar(jwt);
    }

    @PutMapping("/config")
    public Map<String, Object> config(@AuthenticationPrincipal Jwt jwt, @RequestBody Map<String, Object> body) {
        return locaisSegurosService.atualizarConfig(jwt, body);
    }

    @PostMapping
    public Map<String, Object> criar(@AuthenticationPrincipal Jwt jwt, @RequestBody Map<String, Object> body) {
        return locaisSegurosService.criar(jwt, body);
    }

    @PutMapping("/{id}")
    public Map<String, Object> atualizar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @RequestBody Map<String, Object> body
    ) {
        return locaisSegurosService.atualizar(jwt, id, body);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        return locaisSegurosService.excluir(jwt, id);
    }
}

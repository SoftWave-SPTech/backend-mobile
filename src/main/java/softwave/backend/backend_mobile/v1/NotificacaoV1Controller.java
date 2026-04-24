package softwave.backend.backend_mobile.v1;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import softwave.backend.backend_mobile.service.V1NotificacaoService;

import java.util.Map;

@RestController
@RequestMapping("/v1/notificacoes")
public class NotificacaoV1Controller {

    private final V1NotificacaoService notificacaoService;

    public NotificacaoV1Controller(V1NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public Map<String, Object> listar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return notificacaoService.listarAdvogado(jwt, page, limit);
    }

    @PutMapping("/{id}/lida")
    public Map<String, Object> lida(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        return notificacaoService.marcarLidaAdvogado(jwt, id);
    }

    @PutMapping("/marcar-todas-lidas")
    public Map<String, Object> todasLidas(@AuthenticationPrincipal Jwt jwt) {
        return notificacaoService.marcarTodasLidas(jwt);
    }
}

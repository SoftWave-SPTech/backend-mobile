package softwave.backend.backend_mobile.v1;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import softwave.backend.backend_mobile.Service.V1ProcessoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/processos")
public class ProcessosV1Controller {

    private final V1ProcessoService processoService;

    public ProcessosV1Controller(V1ProcessoService processoService) {
        this.processoService = processoService;
    }

    @GetMapping
    public List<Map<String, Object>> listar(@AuthenticationPrincipal Jwt jwt) {
        return processoService.listar(jwt);
    }
}

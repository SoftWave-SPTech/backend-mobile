package softwave.backend.backend_mobile.v1;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import softwave.backend.backend_mobile.service.V1DashboardService;

import java.util.Map;

@RestController
@RequestMapping("/v1/dashboard")
public class DashboardController {

    private final V1DashboardService dashboardService;

    public DashboardController(V1DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumo")
    public Map<String, Object> resumo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false, defaultValue = "mes") String periodo
    ) {
        return dashboardService.resumo(jwt, periodo);
    }

    @GetMapping("/transacoes-recentes")
    public Map<String, Object> recentes(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false, defaultValue = "3") int limit
    ) {
        return dashboardService.transacoesRecentes(jwt, limit);
    }
}

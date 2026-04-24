package softwave.backend.backend_mobile.v1;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import softwave.backend.backend_mobile.service.V1RelatorioService;

import java.util.Map;

@RestController
@RequestMapping("/v1/relatorios")
public class RelatorioV1Controller {

    private final V1RelatorioService relatorioService;

    public RelatorioV1Controller(V1RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/receita-despesa")
    public Map<String, Object> receitaDespesa(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false, defaultValue = "mes") String periodo
    ) {
        return relatorioService.receitaDespesa(jwt, periodo);
    }

    @GetMapping("/receita-categoria")
    public Map<String, Object> receitaCategoria(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false, defaultValue = "mes") String periodo
    ) {
        return relatorioService.receitaCategoria(jwt, periodo);
    }

    @GetMapping("/despesas-mes")
    public Map<String, Object> despesasMes(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false, defaultValue = "mes") String periodo
    ) {
        return relatorioService.despesasMes(jwt, periodo);
    }

    @GetMapping("/kpis")
    public Map<String, Object> kpis(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false, defaultValue = "mes") String periodo
    ) {
        return relatorioService.kpis(jwt, periodo);
    }

    @GetMapping("/ranking-clientes")
    public Map<String, Object> ranking(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false, defaultValue = "mes") String periodo,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return relatorioService.rankingClientes(jwt, periodo, limit);
    }
}

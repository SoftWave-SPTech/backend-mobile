package softwave.backend.backend_mobile.internal;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import softwave.backend.backend_mobile.internal.dto.CobrancaResumoInternoDto;
import softwave.backend.backend_mobile.internal.dto.RankingReceitaResponseDto;
import softwave.backend.backend_mobile.internal.dto.TransacaoResumoInternoDto;
import softwave.backend.backend_mobile.service.FinanceiroInternoService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal/v1/tenants/{tenantId}")
public class FinanceiroInternoController {

    private final FinanceiroInternoService financeiroInternoService;

    public FinanceiroInternoController(FinanceiroInternoService financeiroInternoService) {
        this.financeiroInternoService = financeiroInternoService;
    }

    @GetMapping("/transacoes/resumo")
    public TransacaoResumoInternoDto transacoesResumo(
            @PathVariable long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return financeiroInternoService.transacoesResumo(tenantId, dataInicio, dataFim);
    }

    @GetMapping("/cobrancas/resumo")
    public CobrancaResumoInternoDto cobrancasResumo(
            @PathVariable long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return financeiroInternoService.cobrancasResumo(tenantId, dataInicio, dataFim);
    }

    @GetMapping("/clientes/ranking-receita")
    public RankingReceitaResponseDto ranking(
            @PathVariable long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(defaultValue = "10") int limite
    ) {
        return financeiroInternoService.rankingReceita(tenantId, dataInicio, dataFim, limite);
    }

    @GetMapping("/financeiro/kpis")
    public Map<String, Object> kpisAgregados(
            @PathVariable long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        TransacaoResumoInternoDto t = financeiroInternoService.transacoesResumo(tenantId, dataInicio, dataFim);
        CobrancaResumoInternoDto c = financeiroInternoService.cobrancasResumo(tenantId, dataInicio, dataFim);
        RankingReceitaResponseDto r = financeiroInternoService.rankingReceita(tenantId, dataInicio, dataFim, 10);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("transacoes", t);
        m.put("cobrancas", c);
        m.put("rankingClientes", r);
        return m;
    }
}

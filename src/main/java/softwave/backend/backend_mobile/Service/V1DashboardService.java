package softwave.backend.backend_mobile.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;
import softwave.backend.backend_mobile.Repository.TransacaoSpecifications;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;
import softwave.backend.backend_mobile.service.ProcessoAccessService;
import softwave.backend.backend_mobile.util.MoneyUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class V1DashboardService {

    private final ProcessoAccessService processoAccessService;
    private final TransacaoRepository transacaoRepository;

    public V1DashboardService(ProcessoAccessService processoAccessService, TransacaoRepository transacaoRepository) {
        this.processoAccessService = processoAccessService;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> resumo(Jwt jwt, String periodo) {
        int uid = JwtPrincipalExtractor.userId(jwt);
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Apenas advogado");
        }
        List<Integer> pids = processoAccessService.processoIdsDoUsuario(uid);
        LocalDate[] range = periodoParaDatas(periodo);
        LocalDate ini = range[0];
        LocalDate fim = range[1];

        List<TransacaoEntity> tx = transacaoRepository.findAll(
                TransacaoSpecifications.processoEm(pids)
                        .and(TransacaoSpecifications.dataEmissaoEntre(ini, fim))
        );

        BigDecimal receita = BigDecimal.ZERO;
        BigDecimal despesa = BigDecimal.ZERO;
        BigDecimal pendente = BigDecimal.ZERO;

        for (TransacaoEntity t : tx) {
            BigDecimal v = MoneyUtil.toBigDecimalOrZero(t.getValor());
            if (isReceita(t)) {
                receita = receita.add(v);
                if (!estaPago(t)) {
                    pendente = pendente.add(v);
                }
            } else if (isDespesa(t)) {
                despesa = despesa.add(v);
            }
        }

        long pendentesConf = transacaoRepository.countByHonorario_Processo_IdInAndStatusAprovacaoIgnoreCase(
                pids, "pendente"
        );

        Map<String, Object> body = new HashMap<>();
        body.put("valorDisponivel", MoneyUtil.toCentavos(receita.subtract(despesa)));
        body.put("lucroLiquidoMes", MoneyUtil.toCentavos(receita.subtract(despesa)));
        body.put("receitaMensal", MoneyUtil.toCentavos(receita));
        body.put("despesaMensal", MoneyUtil.toCentavos(despesa));
        body.put("pendentes", MoneyUtil.toCentavos(pendente));
        body.put("variacaoReceita", "+0%");
        body.put("variacaoDespesa", "+0%");
        body.put("variacaoLucro", "+0%");
        body.put("pagamentosParaConferir", pendentesConf);
        return body;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> transacoesRecentes(Jwt jwt, int limit) {
        int uid = JwtPrincipalExtractor.userId(jwt);
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Apenas advogado");
        }
        List<Integer> pids = processoAccessService.processoIdsDoUsuario(uid);
        if (pids.isEmpty()) {
            return Map.of("transacoes", List.of());
        }
        var page = transacaoRepository.findAll(
                TransacaoSpecifications.processoEm(pids),
                PageRequest.of(0, Math.max(1, Math.min(limit, 50)), Sort.by(Sort.Direction.DESC, "dataEmissao"))
        );
        List<Map<String, Object>> list = page.getContent().stream().map(this::mapTransacaoLista).toList();
        return Map.of("transacoes", list);
    }

    private Map<String, Object> mapTransacaoLista(TransacaoEntity t) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", "txn_" + t.getId());
        m.put("titulo", t.getTitulo() != null ? t.getTitulo() : "");
        m.put("subtitulo", t.getContraparte());
        m.put("valor", MoneyUtil.toCentavos(MoneyUtil.toBigDecimalOrZero(t.getValor())));
        m.put("tipo", mapTipoApi(t));
        m.put("status", mapStatusApi(t));
        m.put("categoria", "honorarios");
        m.put("data", t.getDataEmissao() != null ? t.getDataEmissao().toString() : null);
        m.put("icone", "cash");
        return m;
    }

    private static String mapTipoApi(TransacaoEntity t) {
        if (isReceita(t)) {
            return "receita";
        }
        if (isDespesa(t)) {
            return "despesa";
        }
        return "receita";
    }

    private static String mapStatusApi(TransacaoEntity t) {
        if (estaPago(t)) {
            return "pago";
        }
        if (t.getDataVencimento() != null && t.getDataVencimento().isBefore(LocalDate.now())) {
            return "atrasado";
        }
        return "pendente";
    }

    private static boolean isReceita(TransacaoEntity t) {
        String tipo = t.getTipo();
        return tipo != null && tipo.toLowerCase(Locale.ROOT).contains("receita");
    }

    private static boolean isDespesa(TransacaoEntity t) {
        String tipo = t.getTipo();
        return tipo != null && tipo.toLowerCase(Locale.ROOT).contains("despesa");
    }

    private static boolean estaPago(TransacaoEntity t) {
        if (t.getDataPagamento() != null) {
            return true;
        }
        String sf = t.getStatusFinanceiro();
        return sf != null && sf.toLowerCase(Locale.ROOT).contains("pago");
    }

    private static LocalDate[] periodoParaDatas(String periodo) {
        LocalDate hoje = LocalDate.now();
        if ("semana".equalsIgnoreCase(periodo)) {
            LocalDate ini = hoje.minusDays(7);
            return new LocalDate[]{ini, hoje};
        }
        if ("ano".equalsIgnoreCase(periodo)) {
            LocalDate ini = hoje.withDayOfYear(1);
            return new LocalDate[]{ini, hoje};
        }
        LocalDate ini = hoje.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fim = hoje.with(TemporalAdjusters.lastDayOfMonth());
        return new LocalDate[]{ini, fim};
    }
}

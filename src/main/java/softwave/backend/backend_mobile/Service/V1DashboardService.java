package softwave.backend.backend_mobile.Service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;
import softwave.backend.backend_mobile.Repository.TransacaoSpecifications;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;
import softwave.backend.backend_mobile.Service.ProcessoAccessService;
import softwave.backend.backend_mobile.util.MoneyUtil;
import softwave.backend.backend_mobile.util.TransacaoFinanceiroRules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
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

        Specification<TransacaoEntity> acesso = pids.isEmpty()
                ? TransacaoSpecifications.avulsoDoAdvogado(uid)
                : TransacaoSpecifications.processoEmOuAvulso(pids, uid);
        List<TransacaoEntity> tx = transacaoRepository.findAll(acesso.and(TransacaoSpecifications.dataEmissaoEntre(ini, fim)));

        BigDecimal receitaPago = BigDecimal.ZERO;
        BigDecimal despesaPago = BigDecimal.ZERO;
        BigDecimal pendente = BigDecimal.ZERO;

        for (TransacaoEntity t : tx) {
            if (TransacaoFinanceiroRules.isCancelada(t)) {
                continue;
            }
            BigDecimal v = MoneyUtil.toBigDecimalOrZero(t.getValor());
            if (TransacaoFinanceiroRules.isReceita(t)) {
                if (TransacaoFinanceiroRules.estaPago(t)) {
                    receitaPago = receitaPago.add(v);
                } else {
                    pendente = pendente.add(v);
                }
            } else if (TransacaoFinanceiroRules.isDespesa(t) && TransacaoFinanceiroRules.estaPago(t)) {
                despesaPago = despesaPago.add(v);
            }
        }

        long pendentesConf = transacaoRepository.countByHonorario_Processo_IdInAndStatusAprovacaoIgnoreCase(
                pids, "pendente"
        );
        BigDecimal valorDisponivel = receitaPago.subtract(despesaPago);

        Map<String, Object> body = new HashMap<>();
        body.put("valorDisponivel", MoneyUtil.toCentavos(valorDisponivel));
        body.put("lucroLiquidoMes", MoneyUtil.toCentavos(valorDisponivel));
        body.put("receitaMensal", MoneyUtil.toCentavos(receitaPago));
        body.put("despesaMensal", MoneyUtil.toCentavos(despesaPago));
        body.put("pendentes", MoneyUtil.toCentavos(pendente));
        body.put("variacaoReceita", "+0%");
        body.put("variacaoPendentes", "+0%");
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
        Specification<TransacaoEntity> acesso = pids.isEmpty()
                ? TransacaoSpecifications.avulsoDoAdvogado(uid)
                : TransacaoSpecifications.processoEmOuAvulso(pids, uid);
        var page = transacaoRepository.findAll(
                acesso,
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
        m.put("categoria", TransacaoFinanceiroRules.categoriaOrDefault(t));
        m.put("data", t.getDataEmissao() != null ? t.getDataEmissao().toString() : null);
        m.put("icone", "cash");
        return m;
    }

    private static String mapTipoApi(TransacaoEntity t) {
        if (TransacaoFinanceiroRules.isReceita(t)) {
            return "receita";
        }
        if (TransacaoFinanceiroRules.isDespesa(t)) {
            return "despesa";
        }
        return "receita";
    }

    private static String mapStatusApi(TransacaoEntity t) {
        if (TransacaoFinanceiroRules.isCancelada(t)) {
            return "cancelado";
        }
        if (TransacaoFinanceiroRules.estaPago(t)) {
            return "pago";
        }
        if (t.getDataVencimento() != null && t.getDataVencimento().isBefore(LocalDate.now())) {
            return "atrasado";
        }
        return "pendente";
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

package softwave.backend.backend_mobile.Service;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;
import softwave.backend.backend_mobile.Repository.TransacaoSpecifications;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;
import softwave.backend.backend_mobile.util.MoneyUtil;
import softwave.backend.backend_mobile.util.TransacaoFinanceiroRules;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class V1RelatorioService {

    private final TransacaoRepository transacaoRepository;
    private final ProcessoAccessService processoAccessService;

    public V1RelatorioService(TransacaoRepository transacaoRepository, ProcessoAccessService processoAccessService) {
        this.transacaoRepository = transacaoRepository;
        this.processoAccessService = processoAccessService;
    }

    private List<Integer> pids(Jwt jwt) {
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Apenas advogado");
        }
        return processoAccessService.processoIdsDoUsuario(JwtPrincipalExtractor.userId(jwt));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> receitaDespesa(Jwt jwt, String periodo) {
        LocalDate[] r = range(periodo);
        int uid = JwtPrincipalExtractor.userId(jwt);
        List<Integer> pids = pids(jwt);
        Specification<TransacaoEntity> acesso = pids.isEmpty()
                ? TransacaoSpecifications.avulsoDoAdvogado(uid)
                : TransacaoSpecifications.processoEmOuAvulso(pids, uid);
        List<TransacaoEntity> tx = transacaoRepository.findAll(acesso.and(TransacaoSpecifications.dataEmissaoEntre(r[0], r[1])));
        List<String> labels = List.of("P1", "P2", "P3");
        List<Long> receita = new ArrayList<>();
        List<Long> despesa = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            receita.add(0L);
            despesa.add(0L);
        }
        long rec = 0, des = 0;
        for (TransacaoEntity t : tx) {
            if (TransacaoFinanceiroRules.isCancelada(t) || !TransacaoFinanceiroRules.estaPago(t)) {
                continue;
            }
            BigDecimal v = MoneyUtil.toBigDecimalOrZero(t.getValor());
            long c = MoneyUtil.toCentavos(v);
            if (TransacaoFinanceiroRules.isReceita(t)) {
                rec += c;
            } else if (TransacaoFinanceiroRules.isDespesa(t)) {
                des += c;
            }
        }
        receita.set(2, rec);
        despesa.set(2, des);
        return Map.of("labels", labels, "receita", receita, "despesa", despesa);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> receitaCategoria(Jwt jwt, String periodo) {
        LocalDate[] r = range(periodo);
        int uid = JwtPrincipalExtractor.userId(jwt);
        List<Integer> pids = pids(jwt);
        Specification<TransacaoEntity> acesso = pids.isEmpty()
                ? TransacaoSpecifications.avulsoDoAdvogado(uid)
                : TransacaoSpecifications.processoEmOuAvulso(pids, uid);
        List<TransacaoEntity> tx = transacaoRepository.findAll(acesso.and(TransacaoSpecifications.dataEmissaoEntre(r[0], r[1])));
        Map<String, Long> acc = new HashMap<>();
        long total = 0;
        for (TransacaoEntity t : tx) {
            if (TransacaoFinanceiroRules.isCancelada(t) || !TransacaoFinanceiroRules.estaPago(t) || !TransacaoFinanceiroRules.isReceita(t)) {
                continue;
            }
            long c = MoneyUtil.toCentavos(MoneyUtil.toBigDecimalOrZero(t.getValor()));
            String nome = TransacaoFinanceiroRules.categoriaOrDefault(t);
            acc.merge(nome, c, Long::sum);
            total += c;
        }
        List<Map<String, Object>> cats = new ArrayList<>();
        for (Map.Entry<String, Long> e : acc.entrySet()) {
            int pct = total > 0 ? (int) (100 * e.getValue() / total) : 0;
            cats.add(Map.of("nome", e.getKey(), "valor", e.getValue(), "percentual", pct));
        }
        return Map.of("categorias", cats);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> despesasMes(Jwt jwt, String periodo) {
        return receitaDespesa(jwt, periodo);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> kpis(Jwt jwt, String periodo) {
        LocalDate[] r = range(periodo);
        int uid = JwtPrincipalExtractor.userId(jwt);
        List<Integer> pids = pids(jwt);
        Specification<TransacaoEntity> acesso = pids.isEmpty()
                ? TransacaoSpecifications.avulsoDoAdvogado(uid)
                : TransacaoSpecifications.processoEmOuAvulso(pids, uid);
        List<TransacaoEntity> tx = transacaoRepository.findAll(acesso.and(TransacaoSpecifications.dataEmissaoEntre(r[0], r[1])));
        BigDecimal rec = BigDecimal.ZERO;
        BigDecimal des = BigDecimal.ZERO;
        for (TransacaoEntity t : tx) {
            if (TransacaoFinanceiroRules.isCancelada(t) || !TransacaoFinanceiroRules.estaPago(t)) {
                continue;
            }
            BigDecimal v = MoneyUtil.toBigDecimalOrZero(t.getValor());
            if (TransacaoFinanceiroRules.isReceita(t)) {
                rec = rec.add(v);
            } else if (TransacaoFinanceiroRules.isDespesa(t)) {
                des = des.add(v);
            }
        }
        String margem = rec.signum() == 0 ? "0%" : rec.subtract(des).multiply(BigDecimal.valueOf(100))
                .divide(rec, 1, java.math.RoundingMode.HALF_UP) + "%";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("margemLucro", Map.of("valor", margem, "variacao", "+0%", "tipo", "positivo"));
        body.put("ticketMedio", Map.of("valor", MoneyUtil.toCentavos(rec.max(BigDecimal.ONE)), "variacao", "+0%", "tipo", "positivo"));
        body.put("inadimplencia", Map.of("valor", "0%", "variacao", "+0%", "tipo", "positivo"));
        body.put("crescimento", Map.of("valor", "+0%", "variacao", "+0%", "tipo", "positivo"));
        return body;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> rankingClientes(Jwt jwt, String periodo, int limit) {
        LocalDate[] r = range(periodo);
        List<Integer> pids = pids(jwt);
        var rows = transacaoRepository.rankingReceita(pids, r[0], r[1], org.springframework.data.domain.PageRequest.of(0, Math.min(limit, 50)));
        List<Map<String, Object>> clientes = new ArrayList<>();
        for (Object[] row : rows) {
            BigDecimal brl = row[2] instanceof BigDecimal b ? b : BigDecimal.valueOf(((Number) row[2]).doubleValue());
            clientes.add(Map.of(
                    "id", "cli_" + row[0],
                    "nome", row[1],
                    "valor", MoneyUtil.toCentavos(brl)
            ));
        }
        return Map.of("clientes", clientes);
    }

    private static LocalDate[] range(String periodo) {
        LocalDate hoje = LocalDate.now();
        LocalDate ini = hoje.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fim = hoje.with(TemporalAdjusters.lastDayOfMonth());
        if ("ano".equalsIgnoreCase(periodo)) {
            ini = hoje.withDayOfYear(1);
        }
        return new LocalDate[]{ini, fim};
    }
}

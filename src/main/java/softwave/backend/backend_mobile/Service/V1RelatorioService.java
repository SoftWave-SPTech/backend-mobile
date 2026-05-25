package softwave.backend.backend_mobile.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

@Service
public class V1RelatorioService {

    private static final int MESES_GRAFICO_PADRAO = 6;

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

    private Specification<TransacaoEntity> acesso(Jwt jwt) {
        int uid = JwtPrincipalExtractor.userId(jwt);
        List<Integer> pids = pids(jwt);
        return pids.isEmpty()
                ? TransacaoSpecifications.avulsoDoAdvogado(uid)
                : TransacaoSpecifications.processoEmOuAvulso(pids, uid);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> receitaDespesa(Jwt jwt, String periodo) {
        return serieMensal(jwt, mesesParaGrafico(periodo));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> receitaCategoria(Jwt jwt, String periodo) {
        LocalDate[] r = range(periodo);
        List<TransacaoEntity> tx = transacaoRepository.findAll(
                acesso(jwt).and(TransacaoSpecifications.dataEmissaoEntre(r[0], r[1])));
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
        Map<String, Object> serie = serieMensal(jwt, MESES_GRAFICO_PADRAO);
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) serie.get("labels");
        @SuppressWarnings("unchecked")
        List<Long> despesa = (List<Long>) serie.get("despesa");
        return Map.of("labels", labels, "despesas", despesa);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> kpis(Jwt jwt, String periodo) {
        LocalDate[] r = range(periodo);
        LocalDate[] rAnterior = rangeAnterior(periodo, r);
        List<TransacaoEntity> tx = transacaoRepository.findAll(
                acesso(jwt).and(TransacaoSpecifications.dataEmissaoEntre(r[0], r[1])));
        List<TransacaoEntity> txAnterior = transacaoRepository.findAll(
                acesso(jwt).and(TransacaoSpecifications.dataEmissaoEntre(rAnterior[0], rAnterior[1])));
        List<TransacaoEntity> txTodas = transacaoRepository.findAll(acesso(jwt));

        BigDecimal rec = BigDecimal.ZERO;
        BigDecimal des = BigDecimal.ZERO;
        int qtdReceitasPagas = 0;
        for (TransacaoEntity t : tx) {
            if (TransacaoFinanceiroRules.isCancelada(t) || !TransacaoFinanceiroRules.estaPago(t)) {
                continue;
            }
            BigDecimal v = MoneyUtil.toBigDecimalOrZero(t.getValor());
            if (TransacaoFinanceiroRules.isReceita(t)) {
                rec = rec.add(v);
                qtdReceitasPagas++;
            } else if (TransacaoFinanceiroRules.isDespesa(t)) {
                des = des.add(v);
            }
        }

        BigDecimal recAnterior = somaReceitaPaga(txAnterior);
        long ticketCent = qtdReceitasPagas > 0
                ? MoneyUtil.toCentavos(rec.divide(BigDecimal.valueOf(qtdReceitasPagas), 2, RoundingMode.HALF_UP))
                : 0L;

        BigDecimal pendenteTotal = BigDecimal.ZERO;
        BigDecimal atrasado = BigDecimal.ZERO;
        LocalDate hoje = LocalDate.now();
        for (TransacaoEntity t : txTodas) {
            if (TransacaoFinanceiroRules.isCancelada(t) || !TransacaoFinanceiroRules.isReceita(t) || TransacaoFinanceiroRules.estaPago(t)) {
                continue;
            }
            BigDecimal v = MoneyUtil.toBigDecimalOrZero(t.getValor());
            pendenteTotal = pendenteTotal.add(v);
            if (t.getDataVencimento() != null && t.getDataVencimento().isBefore(hoje)) {
                atrasado = atrasado.add(v);
            }
        }
        String inadimplencia = pendenteTotal.signum() == 0 ? "0%"
                : atrasado.multiply(BigDecimal.valueOf(100)).divide(pendenteTotal, 1, RoundingMode.HALF_UP) + "%";

        String crescimento = formatCrescimento(rec, recAnterior);
        String margem = rec.signum() == 0 ? "0%"
                : rec.subtract(des).multiply(BigDecimal.valueOf(100)).divide(rec, 1, RoundingMode.HALF_UP) + "%";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("margemLucro", kpiItem(margem, "+0%", "positivo"));
        body.put("ticketMedio", kpiItem(String.valueOf(ticketCent), "+0%", "positivo"));
        body.put("inadimplencia", kpiItem(inadimplencia, "+0%", inadimplencia.equals("0%") ? "positivo" : "negativo"));
        body.put("crescimento", kpiItem(crescimento, "+0%", crescimento.startsWith("-") ? "negativo" : "positivo"));
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

    private Map<String, Object> serieMensal(Jwt jwt, int qtdMeses) {
        LocalDate hoje = LocalDate.now();
        YearMonth fimYm = YearMonth.from(hoje);
        YearMonth iniYm = fimYm.minusMonths(qtdMeses - 1L);
        LocalDate ini = iniYm.atDay(1);
        LocalDate fim = fimYm.atEndOfMonth();

        List<TransacaoEntity> tx = transacaoRepository.findAll(
                acesso(jwt).and(TransacaoSpecifications.dataEmissaoEntre(ini, fim)));

        Map<YearMonth, long[]> porMes = new LinkedHashMap<>();
        for (int i = 0; i < qtdMeses; i++) {
            porMes.put(iniYm.plusMonths(i), new long[]{0L, 0L});
        }

        for (TransacaoEntity t : tx) {
            if (TransacaoFinanceiroRules.isCancelada(t) || !TransacaoFinanceiroRules.estaPago(t) || t.getDataEmissao() == null) {
                continue;
            }
            YearMonth ym = YearMonth.from(t.getDataEmissao());
            long[] bucket = porMes.get(ym);
            if (bucket == null) {
                continue;
            }
            long c = MoneyUtil.toCentavos(MoneyUtil.toBigDecimalOrZero(t.getValor()));
            if (TransacaoFinanceiroRules.isReceita(t)) {
                bucket[0] += c;
            } else if (TransacaoFinanceiroRules.isDespesa(t)) {
                bucket[1] += c;
            }
        }

        List<String> labels = new ArrayList<>();
        List<Long> receita = new ArrayList<>();
        List<Long> despesa = new ArrayList<>();
        for (Map.Entry<YearMonth, long[]> e : porMes.entrySet()) {
            String mes = e.getKey().getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
            labels.add(mes.substring(0, 1).toUpperCase() + mes.substring(1));
            receita.add(e.getValue()[0]);
            despesa.add(e.getValue()[1]);
        }
        return Map.of("labels", labels, "receita", receita, "despesa", despesa);
    }

    private static BigDecimal somaReceitaPaga(List<TransacaoEntity> tx) {
        BigDecimal rec = BigDecimal.ZERO;
        for (TransacaoEntity t : tx) {
            if (TransacaoFinanceiroRules.isCancelada(t) || !TransacaoFinanceiroRules.estaPago(t) || !TransacaoFinanceiroRules.isReceita(t)) {
                continue;
            }
            rec = rec.add(MoneyUtil.toBigDecimalOrZero(t.getValor()));
        }
        return rec;
    }

    private static String formatCrescimento(BigDecimal atual, BigDecimal anterior) {
        if (anterior.signum() == 0) {
            return atual.signum() > 0 ? "+100%" : "+0%";
        }
        BigDecimal pct = atual.subtract(anterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(anterior, 1, RoundingMode.HALF_UP);
        return (pct.signum() >= 0 ? "+" : "") + pct + "%";
    }

    private static Map<String, Object> kpiItem(String valor, String variacao, String tipo) {
        return Map.of("valor", valor, "variacao", variacao, "tipo", tipo);
    }

    private static int mesesParaGrafico(String periodo) {
        if ("ano".equalsIgnoreCase(periodo)) {
            return 12;
        }
        if ("semestre".equalsIgnoreCase(periodo)) {
            return 6;
        }
        return MESES_GRAFICO_PADRAO;
    }

    private static LocalDate[] range(String periodo) {
        LocalDate hoje = LocalDate.now();
        LocalDate ini = hoje.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fim = hoje.with(TemporalAdjusters.lastDayOfMonth());
        if ("semestre".equalsIgnoreCase(periodo)) {
            ini = hoje.minusMonths(5).with(TemporalAdjusters.firstDayOfMonth());
            fim = hoje.with(TemporalAdjusters.lastDayOfMonth());
        } else if ("ano".equalsIgnoreCase(periodo)) {
            ini = hoje.withDayOfYear(1);
        }
        return new LocalDate[]{ini, fim};
    }

    private static LocalDate[] rangeAnterior(String periodo, LocalDate[] atual) {
        if ("ano".equalsIgnoreCase(periodo)) {
            LocalDate ini = atual[0].minusYears(1);
            LocalDate fim = atual[1].minusYears(1);
            return new LocalDate[]{ini, fim};
        }
        if ("semestre".equalsIgnoreCase(periodo)) {
            LocalDate ini = atual[0].minusMonths(6);
            LocalDate fim = atual[1].minusMonths(6);
            return new LocalDate[]{ini, fim};
        }
        LocalDate ini = atual[0].minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fim = ini.with(TemporalAdjusters.lastDayOfMonth());
        return new LocalDate[]{ini, fim};
    }
}

package softwave.backend.backend_mobile.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Entity.UsuarioEntity;
import softwave.backend.backend_mobile.Exception.BadRequestException;
import softwave.backend.backend_mobile.Exception.NotFoundException;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;
import softwave.backend.backend_mobile.Repository.UsuarioRepository;
import softwave.backend.backend_mobile.internal.dto.CobrancaResumoInternoDto;
import softwave.backend.backend_mobile.internal.dto.RankingClienteItemDto;
import softwave.backend.backend_mobile.internal.dto.RankingReceitaResponseDto;
import softwave.backend.backend_mobile.internal.dto.TransacaoResumoInternoDto;
import softwave.backend.backend_mobile.util.MoneyUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class FinanceiroInternoService {

    private final UsuarioRepository usuarioRepository;
    private final ProcessoAccessService processoAccessService;
    private final TransacaoRepository transacaoRepository;

    public FinanceiroInternoService(
            UsuarioRepository usuarioRepository,
            ProcessoAccessService processoAccessService,
            TransacaoRepository transacaoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.processoAccessService = processoAccessService;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public TransacaoResumoInternoDto transacoesResumo(long tenantId, LocalDate ini, LocalDate fim) {
        validarPeriodo(ini, fim);
        UsuarioEntity tenant = tenantAdvogado((int) tenantId);
        List<Integer> pids = processoAccessService.processoIdsDoUsuario(tenant.getId());
        if (pids.isEmpty()) {
            return new TransacaoResumoInternoDto(
                    tenantId, ini, fim, 0, 0, 0, 0,
                    Map.of(), Map.of()
            );
        }

        List<TransacaoEntity> tx = transacaoRepository.findByProcessoInAndDataEmissaoBetween(pids, ini, fim);
        BigDecimal receita = BigDecimal.ZERO;
        BigDecimal despesa = BigDecimal.ZERO;
        Map<String, Double> recCat = new HashMap<>();
        Map<String, Double> desCat = new HashMap<>();

        for (TransacaoEntity t : tx) {
            BigDecimal v = MoneyUtil.toBigDecimalOrZero(t.getValor());
            String cat = categoriaChave(t);
            if (isReceita(t)) {
                receita = receita.add(v);
                recCat.merge(cat, v.doubleValue(), Double::sum);
            } else if (isDespesa(t)) {
                despesa = despesa.add(v);
                desCat.merge(cat, v.doubleValue(), Double::sum);
            }
        }

        long n = tx.size();
        double ticket = n > 0 ? receita.add(despesa).doubleValue() / n : 0;

        return new TransacaoResumoInternoDto(
                tenantId,
                ini,
                fim,
                receita.doubleValue(),
                despesa.doubleValue(),
                ticket,
                n,
                recCat,
                desCat
        );
    }

    @Transactional(readOnly = true)
    public CobrancaResumoInternoDto cobrancasResumo(long tenantId, LocalDate ini, LocalDate fim) {
        validarPeriodo(ini, fim);
        UsuarioEntity tenant = tenantAdvogado((int) tenantId);
        List<Integer> pids = processoAccessService.processoIdsDoUsuario(tenant.getId());
        if (pids.isEmpty()) {
            return new CobrancaResumoInternoDto(tenantId, ini, fim, 0, 0, 0, 0, 0);
        }

        List<TransacaoEntity> tx = transacaoRepository.findByProcessoInAndDataEmissaoBetween(pids, ini, fim);
        LocalDate hoje = LocalDate.now();
        BigDecimal recebido = BigDecimal.ZERO;
        BigDecimal vencido = BigDecimal.ZERO;
        BigDecimal aVencer = BigDecimal.ZERO;
        long abertos = 0;

        for (TransacaoEntity t : tx) {
            if (!ehCobrancaReceita(t)) {
                continue;
            }
            BigDecimal v = MoneyUtil.toBigDecimalOrZero(t.getValor());
            if (estaPago(t)) {
                recebido = recebido.add(v);
            } else {
                abertos++;
                LocalDate ven = t.getDataVencimento();
                if (ven != null && ven.isBefore(hoje)) {
                    vencido = vencido.add(v);
                } else {
                    aVencer = aVencer.add(v);
                }
            }
        }

        BigDecimal carteira = recebido.add(vencido).add(aVencer);
        double inad = carteira.signum() > 0
                ? vencido.divide(carteira, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0;

        return new CobrancaResumoInternoDto(
                tenantId,
                ini,
                fim,
                inad,
                recebido.doubleValue(),
                vencido.doubleValue(),
                aVencer.doubleValue(),
                abertos
        );
    }

    @Transactional(readOnly = true)
    public RankingReceitaResponseDto rankingReceita(long tenantId, LocalDate ini, LocalDate fim, int limite) {
        validarPeriodo(ini, fim);
        if (limite <= 0) {
            limite = 10;
        }
        if (limite > 50) {
            limite = 50;
        }
        UsuarioEntity tenant = tenantAdvogado((int) tenantId);
        List<Integer> pids = processoAccessService.processoIdsDoUsuario(tenant.getId());
        if (pids.isEmpty()) {
            return new RankingReceitaResponseDto(tenantId, ini, fim, List.of());
        }

        List<Object[]> rows = transacaoRepository.rankingReceita(pids, ini, fim, PageRequest.of(0, limite));
        List<RankingClienteItemDto> itens = rows.stream()
                .map(r -> new RankingClienteItemDto(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        ((BigDecimal) r[2]).doubleValue()
                ))
                .toList();

        return new RankingReceitaResponseDto(tenantId, ini, fim, itens);
    }

    private UsuarioEntity tenantAdvogado(int tenantId) {
        UsuarioEntity u = usuarioRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant não encontrado"));
        if (!u.isAdvogado()) {
            throw new BadRequestException("tenantId deve ser um usuário advogado");
        }
        return u;
    }

    private static void validarPeriodo(LocalDate ini, LocalDate fim) {
        if (ini == null || fim == null || ini.isAfter(fim)) {
            throw new BadRequestException("Período inválido");
        }
    }

    private static boolean isReceita(TransacaoEntity t) {
        String tipo = t.getTipo();
        return tipo != null && tipo.toLowerCase(Locale.ROOT).contains("receita");
    }

    private static boolean isDespesa(TransacaoEntity t) {
        String tipo = t.getTipo();
        return tipo != null && tipo.toLowerCase(Locale.ROOT).contains("despesa");
    }

    private static boolean ehCobrancaReceita(TransacaoEntity t) {
        return isReceita(t);
    }

    private static boolean estaPago(TransacaoEntity t) {
        if (t.getDataPagamento() != null) {
            return true;
        }
        String sf = t.getStatusFinanceiro();
        return sf != null && sf.toLowerCase(Locale.ROOT).contains("pago");
    }

    private static String categoriaChave(TransacaoEntity t) {
        String tipo = t.getTipo();
        if (tipo == null || tipo.isBlank()) {
            return "OUTROS";
        }
        return tipo.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }
}

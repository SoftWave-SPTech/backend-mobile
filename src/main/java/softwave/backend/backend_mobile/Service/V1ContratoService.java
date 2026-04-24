package softwave.backend.backend_mobile.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softwave.backend.backend_mobile.Entity.HonorarioEntity;
import softwave.backend.backend_mobile.Entity.NotificacaoEntity;
import softwave.backend.backend_mobile.Entity.ProcessoEntity;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Entity.UsuarioEntity;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Exception.NotFoundException;
import softwave.backend.backend_mobile.Repository.HonorarioRepository;
import softwave.backend.backend_mobile.Repository.NotificacaoRepository;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;
import softwave.backend.backend_mobile.Repository.UsuarioProcessoRepository;
import softwave.backend.backend_mobile.Repository.UsuarioRepository;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;
import softwave.backend.backend_mobile.util.MoneyUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class V1ContratoService {

    private final HonorarioRepository honorarioRepository;
    private final TransacaoRepository transacaoRepository;
    private final ProcessoAccessService processoAccessService;
    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioProcessoRepository usuarioProcessoRepository;

    public V1ContratoService(
            HonorarioRepository honorarioRepository,
            TransacaoRepository transacaoRepository,
            ProcessoAccessService processoAccessService,
            NotificacaoRepository notificacaoRepository,
            UsuarioProcessoRepository usuarioProcessoRepository
    ) {
        this.honorarioRepository = honorarioRepository;
        this.transacaoRepository = transacaoRepository;
        this.processoAccessService = processoAccessService;
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioProcessoRepository = usuarioProcessoRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listar(Jwt jwt, String status, Integer clienteId) {
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Apenas advogado");
        }
        List<Integer> pids = processoAccessService.processoIdsDoUsuario(JwtPrincipalExtractor.userId(jwt));
        List<HonorarioEntity> lista = honorarioRepository.findByProcesso_IdIn(pids);
        long totalRecebidoCent = 0;
        long aReceberCent = 0;
        List<Map<String, Object>> contratos = new ArrayList<>();
        for (HonorarioEntity h : lista) {
            Map<String, Object> c = mapContrato(h);
            contratos.add(c);
            long pago = ((Number) c.get("pago")).longValue();
            long total = ((Number) c.get("total")).longValue();
            totalRecebidoCent += pago;
            aReceberCent += Math.max(0, total - pago);
        }
        Map<String, Object> resumo = new HashMap<>();
        resumo.put("totalRecebido", totalRecebidoCent);
        resumo.put("aReceber", aReceberCent);
        return Map.of("resumo", resumo, "contratos", contratos);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detalhe(Jwt jwt, String idStr) {
        int id = parseCtr(idStr);
        HonorarioEntity h = honorarioRepository.findById(id).orElseThrow(() -> new NotFoundException("Contrato não encontrado"));
        processoAccessService.garantirAcessoAoProcesso(JwtPrincipalExtractor.userId(jwt), jwt, h.getProcesso().getId());
        return mapContratoDetalhe(h);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> parcelas(Jwt jwt, String idStr) {
        int id = parseCtr(idStr);
        HonorarioEntity h = honorarioRepository.findById(id).orElseThrow(() -> new NotFoundException("Contrato não encontrado"));
        processoAccessService.garantirAcessoAoProcesso(JwtPrincipalExtractor.userId(jwt), jwt, h.getProcesso().getId());
        List<TransacaoEntity> ts = transacaoRepository.findByHonorario_IdOrderByDataEmissaoAsc(id);
        List<Map<String, Object>> parcelas = new ArrayList<>();
        int n = 1;
        for (TransacaoEntity t : ts) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("id", "par_" + t.getId());
            p.put("numero", n++);
            p.put("valor", MoneyUtil.toCentavos(MoneyUtil.toBigDecimalOrZero(t.getValor())));
            p.put("vencimento", t.getDataVencimento() != null ? t.getDataVencimento().toString() : null);
            p.put("status", t.getStatusFinanceiro() != null && t.getStatusFinanceiro().toLowerCase().contains("pago") ? "pago" : "pendente");
            parcelas.add(p);
        }
        return Map.of("parcelas", parcelas);
    }

    @Transactional
    public Map<String, Object> atualizarParcela(Jwt jwt, String parId, String novoStatus) {
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Apenas advogado");
        }
        int tid = parsePar(parId);
        TransacaoEntity t = transacaoRepository.findById(tid).orElseThrow(() -> new NotFoundException("Parcela não encontrada"));
        processoAccessService.garantirAcessoAoProcesso(JwtPrincipalExtractor.userId(jwt), jwt, t.getHonorario().getProcesso().getId());
        t.setStatusFinanceiro(novoStatus);
        transacaoRepository.save(t);
        return Map.of("mensagem", "Parcela atualizada com sucesso.");
    }

    @Transactional
    public Map<String, Object> gerarCobranca(Jwt jwt, String parId) {
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Apenas advogado");
        }
        int tid = parsePar(parId);
        TransacaoEntity t = transacaoRepository.findById(tid).orElseThrow(() -> new NotFoundException("Parcela não encontrada"));
        Integer processoId = t.getHonorario().getProcesso().getId();
        processoAccessService.garantirAcessoAoProcesso(JwtPrincipalExtractor.userId(jwt), jwt, processoId);

        usuarioProcessoRepository.findByIdProcessoIdIn(List.of(processoId)).stream()
                .map(up -> up.getUsuario())
                .filter(UsuarioEntity::isCliente)
                .findFirst()
                .ifPresent(cliente -> {
                    NotificacaoEntity n = new NotificacaoEntity();
                    n.setUsuario(cliente);
                    n.setTitulo("Nova cobrança");
                    n.setMensagem("O escritório gerou uma cobrança referente a uma parcela.");
                    n.setTipo("cobranca");
                    n.setLida(false);
                    n.setDataCriacao(LocalDateTime.now());
                    notificacaoRepository.save(n);
                });

        return Map.of("mensagem", "Cobrança gerada e enviada ao cliente.", "cobrancaId", "cob_" + tid);
    }

    private Map<String, Object> mapContrato(HonorarioEntity h) {
        ProcessoEntity p = h.getProcesso();
        List<TransacaoEntity> ts = h.getTransacoes();
        BigDecimal total = MoneyUtil.toBigDecimalOrZero(h.getValorTotal());
        BigDecimal somaPago = BigDecimal.ZERO;
        for (TransacaoEntity t : ts) {
            if (t.getDataPagamento() != null || (t.getStatusFinanceiro() != null && t.getStatusFinanceiro().toLowerCase().contains("pago"))) {
                somaPago = somaPago.add(MoneyUtil.toBigDecimalOrZero(t.getValor()));
            }
        }
        int progresso = total.signum() > 0
                ? somaPago.multiply(BigDecimal.valueOf(100)).divide(total, 0, RoundingMode.HALF_UP).intValue()
                : 0;

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "ctr_" + h.getId());
        m.put("clienteId", primeiroClienteId(p.getId()));
        m.put("cliente", primeiroClienteNome(p.getId()));
        m.put("processo", p.getTituloExibicao());
        m.put("tipoContrato", h.getTitulo() != null ? h.getTitulo() : "Parcelas");
        m.put("status", h.getStatus() != null ? h.getStatus() : "em-dia");
        m.put("progresso", Math.min(100, progresso));
        m.put("vencimento", h.getDataFim() != null ? h.getDataFim().toString() : null);
        m.put("total", MoneyUtil.toCentavos(total));
        m.put("pago", MoneyUtil.toCentavos(somaPago));
        m.put("encerrado", Boolean.FALSE);
        m.put("reprovado", Boolean.FALSE);
        return m;
    }

    private String primeiroClienteId(Integer processoId) {
        return usuarioProcessoRepository.findByIdProcessoIdIn(List.of(processoId)).stream()
                .map(up -> up.getUsuario())
                .filter(UsuarioEntity::isCliente)
                .findFirst()
                .map(u -> "cli_" + u.getId())
                .orElse("cli_?");
    }

    private String primeiroClienteNome(Integer processoId) {
        return usuarioProcessoRepository.findByIdProcessoIdIn(List.of(processoId)).stream()
                .map(up -> up.getUsuario())
                .filter(UsuarioEntity::isCliente)
                .map(u -> u.getNome() != null ? u.getNome() : u.getEmail())
                .findFirst()
                .orElse("");
    }

    private Map<String, Object> mapContratoDetalhe(HonorarioEntity h) {
        Map<String, Object> m = new LinkedHashMap<>(mapContrato(h));
        m.put("descricao", h.getTitulo());
        m.put("criadoEm", h.getDataInicio() != null ? h.getDataInicio().toString() : null);
        return m;
    }

    private static int parseCtr(String s) {
        if (s.startsWith("ctr_")) {
            return Integer.parseInt(s.substring(4));
        }
        return Integer.parseInt(s);
    }

    private static int parsePar(String s) {
        if (s.startsWith("par_")) {
            return Integer.parseInt(s.substring(4));
        }
        return Integer.parseInt(s);
    }
}

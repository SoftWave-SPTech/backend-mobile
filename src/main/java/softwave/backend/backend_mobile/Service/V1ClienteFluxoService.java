package softwave.backend.backend_mobile.Service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import softwave.backend.backend_mobile.Entity.ComprovanteEntity;
import softwave.backend.backend_mobile.Entity.NotificacaoEntity;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Entity.UsuarioEntity;
import softwave.backend.backend_mobile.Exception.BadRequestException;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Exception.NotFoundException;
import softwave.backend.backend_mobile.Repository.ComprovanteRepository;
import softwave.backend.backend_mobile.Repository.NotificacaoRepository;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;
import softwave.backend.backend_mobile.Repository.UsuarioProcessoRepository;
import softwave.backend.backend_mobile.Repository.UsuarioRepository;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;
import softwave.backend.backend_mobile.util.MoneyUtil;
import softwave.backend.backend_mobile.util.TransacaoFinanceiroRules;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.*;

@Service
public class V1ClienteFluxoService {

    private final ProcessoAccessService processoAccessService;
    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LocalStorageService localStorageService;
    private final NotificacaoRepository notificacaoRepository;
    private final ComprovanteRepository comprovanteRepository;
    private final UsuarioProcessoRepository usuarioProcessoRepository;

    public V1ClienteFluxoService(
            ProcessoAccessService processoAccessService,
            TransacaoRepository transacaoRepository,
            UsuarioRepository usuarioRepository,
            LocalStorageService localStorageService,
            NotificacaoRepository notificacaoRepository,
            ComprovanteRepository comprovanteRepository,
            UsuarioProcessoRepository usuarioProcessoRepository
    ) {
        this.processoAccessService = processoAccessService;
        this.transacaoRepository = transacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.localStorageService = localStorageService;
        this.notificacaoRepository = notificacaoRepository;
        this.comprovanteRepository = comprovanteRepository;
        this.usuarioProcessoRepository = usuarioProcessoRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard(Jwt jwt) {
        if (!JwtPrincipalExtractor.isCliente(jwt)) {
            throw new ForbiddenException("Área do cliente");
        }
        int uid = JwtPrincipalExtractor.userId(jwt);
        UsuarioEntity u = usuarioRepository.findById(uid).orElseThrow();
        List<Integer> pids = processoAccessService.processoIdsDoUsuario(uid);
        List<TransacaoEntity> ts = transacaoRepository.findByHonorario_Processo_IdIn(pids);

        BigDecimal pago = BigDecimal.ZERO;
        BigDecimal pendente = BigDecimal.ZERO;
        BigDecimal totalCtr = BigDecimal.ZERO;
        TransacaoEntity ultimaCobranca = null;

        for (TransacaoEntity t : ts) {
            if (!TransacaoFinanceiroRules.isReceita(t) || TransacaoFinanceiroRules.isCancelada(t)) {
                continue;
            }
            BigDecimal v = MoneyUtil.toBigDecimalOrZero(t.getValor());
            totalCtr = totalCtr.add(v);
            if (TransacaoFinanceiroRules.estaPago(t)) {
                pago = pago.add(v);
            } else {
                pendente = pendente.add(v);
            }
            if (ultimaCobranca == null || compareRecencia(t, ultimaCobranca) > 0) {
                ultimaCobranca = t;
            }
        }
        int pct = totalCtr.signum() > 0
                ? pago.multiply(BigDecimal.valueOf(100)).divide(totalCtr, 0, java.math.RoundingMode.HALF_UP).intValue()
                : 0;
        long parcelasRestantes = ts.stream()
                .filter(TransacaoFinanceiroRules::isReceita)
                .filter(t -> !TransacaoFinanceiroRules.isCancelada(t))
                .filter(t -> !TransacaoFinanceiroRules.estaPago(t))
                .count();
        long naoLidas = notificacaoRepository.countByUsuario_IdAndLidaIsFalse(uid);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nome", u.getNome());
        body.put("totalPago", MoneyUtil.toCentavos(pago));
        body.put("totalPendente", MoneyUtil.toCentavos(pendente));
        body.put("totalContrato", MoneyUtil.toCentavos(totalCtr));
        body.put("percentualPago", pct);
        body.put("parcelasRestantes", parcelasRestantes);
        body.put("notificacoesNaoLidas", naoLidas);
        if (ultimaCobranca != null) {
            body.put("ultimaCobranca", Map.of(
                    "id", "cob_" + ultimaCobranca.getId(),
                    "descricao", ultimaCobranca.getTitulo() != null ? ultimaCobranca.getTitulo() : "Cobrança",
                    "vencimento", ultimaCobranca.getDataVencimento() != null
                            ? ultimaCobranca.getDataVencimento().toString()
                            : (ultimaCobranca.getDataEmissao() != null ? ultimaCobranca.getDataEmissao().toString() : ""),
                    "valor", MoneyUtil.toCentavos(MoneyUtil.toBigDecimalOrZero(ultimaCobranca.getValor())),
                    "status", TransacaoFinanceiroRules.estaPago(ultimaCobranca) ? "pago" : "pendente"
            ));
        }
        return body;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> cobrancas(Jwt jwt, String status) {
        if (!JwtPrincipalExtractor.isCliente(jwt)) {
            throw new ForbiddenException("Área do cliente");
        }
        List<Integer> pids = processoAccessService.processoIdsDoUsuario(JwtPrincipalExtractor.userId(jwt));
        List<Map<String, Object>> list = new ArrayList<>();
        List<TransacaoEntity> transacoes = transacaoRepository.findByHonorario_Processo_IdIn(pids);
        Map<Integer, List<TransacaoEntity>> porHonorario = transacoes.stream()
                .filter(TransacaoFinanceiroRules::isReceita)
                .filter(t -> !TransacaoFinanceiroRules.isCancelada(t))
                .filter(t -> t.getHonorario() != null)
                .collect(Collectors.groupingBy(t -> t.getHonorario().getId()));

        for (Map.Entry<Integer, List<TransacaoEntity>> entry : porHonorario.entrySet()) {
            List<TransacaoEntity> receitas = entry.getValue().stream()
                    .sorted(Comparator
                            .comparing((TransacaoEntity t) -> t.getDataVencimento() != null ? t.getDataVencimento() : t.getDataEmissao())
                            .thenComparing(TransacaoEntity::getId))
                    .toList();
            if (receitas.isEmpty()) {
                continue;
            }
            int totalParcelas = receitas.size();
            int pagasNoHonorario = (int) receitas.stream().filter(TransacaoFinanceiroRules::estaPago).count();
            int percentualPago = Math.min(100, (int) Math.round((pagasNoHonorario * 100.0) / totalParcelas));

            for (int idx = 0; idx < receitas.size(); idx++) {
                TransacaoEntity t = receitas.get(idx);
                boolean pago = TransacaoFinanceiroRules.estaPago(t);
                if ("pago".equalsIgnoreCase(status) && !pago) {
                    continue;
                }
                if ("pendente".equalsIgnoreCase(status) && pago) {
                    continue;
                }
                if (t.getHonorario() == null || t.getHonorario().getProcesso() == null) {
                    continue;
                }
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", "cob_" + t.getId());
                m.put("processo", t.getHonorario().getProcesso().getTituloExibicao());
                m.put("descricao", t.getTitulo());
                m.put("valor", MoneyUtil.toCentavos(MoneyUtil.toBigDecimalOrZero(t.getValor())));
                m.put("vencimento", t.getDataVencimento() != null ? t.getDataVencimento().toString() : null);
                m.put("status", pago ? "pago" : "pendente");
                m.put("parcela", idx + 1);
                m.put("totalParcelas", totalParcelas);
                m.put("percentualPago", percentualPago);
                list.add(m);
            }
        }
        list.sort(Comparator.comparing(m -> Objects.toString(m.get("vencimento"), "")));
        return Map.of("cobrancas", list);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> cobrancaDetalhe(Jwt jwt, String idStr) {
        TransacaoEntity t = carregarTransacaoCliente(jwt, idStr);
        var comp = comprovanteRepository.findByTransacao_Id(t.getId()).orElse(null);
        List<TransacaoEntity> receitas = transacaoRepository.findByHonorario_IdOrderByDataEmissaoAsc(t.getHonorario().getId()).stream()
                .filter(TransacaoFinanceiroRules::isReceita)
                .filter(tx -> !TransacaoFinanceiroRules.isCancelada(tx))
                .toList();
        int parcela = 1;
        int totalParcelas = receitas.size();
        for (int i = 0; i < receitas.size(); i++) {
            if (Objects.equals(receitas.get(i).getId(), t.getId())) {
                parcela = i + 1;
                break;
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "cob_" + t.getId());
        m.put("processo", t.getHonorario().getProcesso().getTituloExibicao());
        m.put("descricao", t.getTitulo());
        m.put("valor", MoneyUtil.toCentavos(MoneyUtil.toBigDecimalOrZero(t.getValor())));
        m.put("vencimento", t.getDataVencimento() != null ? t.getDataVencimento().toString() : null);
        m.put("status", TransacaoFinanceiroRules.estaPago(t) ? "pago" : "pendente");
        m.put("comprovanteEnviado", comp != null);
        m.put("comprovanteUrl", comp != null ? "/pagamentos/pag_" + t.getId() + "/comprovante" : null);
        m.put("parcela", parcela);
        m.put("totalParcelas", totalParcelas);
        return m;
    }

    private static int compareRecencia(TransacaoEntity a, TransacaoEntity b) {
        var da = a.getDataVencimento() != null ? a.getDataVencimento() : a.getDataEmissao();
        var db = b.getDataVencimento() != null ? b.getDataVencimento() : b.getDataEmissao();
        if (da == null && db == null) {
            return Integer.compare(a.getId(), b.getId());
        }
        if (da == null) {
            return -1;
        }
        if (db == null) {
            return 1;
        }
        int cmp = da.compareTo(db);
        if (cmp != 0) {
            return cmp;
        }
        return Integer.compare(a.getId(), b.getId());
    }

    public Map<String, Object> pixStub(Jwt jwt, String idStr) {
        carregarTransacaoCliente(jwt, idStr);
        return Map.of(
                "pixCopiaCola", "00020126580014BR.GOV.BCB.PIX0136stub",
                "qrCodeBase64", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==",
                "expiresAt", LocalDateTime.now().plusDays(1).toString()
        );
    }

    public Map<String, Object> dadosBancariosEscritorioStub() {
        return Map.of(
                "banco", "Banco do Brasil",
                "agencia", "1234-5",
                "conta", "67890-1",
                "favorecido", "Escritório modelo",
                "cnpj", "12.345.678/0001-90"
        );
    }

    @Transactional
    public Map<String, Object> comprovanteCliente(Jwt jwt, String idStr, MultipartFile arquivo) throws IOException {
        TransacaoEntity t = carregarTransacaoCliente(jwt, idStr);
        if (comprovanteRepository.findByTransacao_Id(t.getId()).isPresent()) {
            throw new BadRequestException("Comprovante já enviado para esta cobrança.");
        }
        String path = localStorageService.salvar(arquivo, "comprovantes-cliente");
        ComprovanteEntity c = new ComprovanteEntity();
        c.setTransacao(t);
        c.setNomeArquivo(arquivo.getOriginalFilename());
        c.setCaminhoArquivo(path);
        c.setDataUpload(LocalDateTime.now());
        comprovanteRepository.save(c);
        t.setStatusAprovacao("pendente");
        transacaoRepository.save(t);
        notificarAdvogadosComprovantePendente(t);
        return Map.of(
                "mensagem", "Comprovante enviado. Aguardando confirmação do escritório.",
                "comprovanteUrl", "file://" + path
        );
    }

    private void notificarAdvogadosComprovantePendente(TransacaoEntity t) {
        if (t.getHonorario() == null || t.getHonorario().getProcesso() == null) {
            return;
        }
        Integer processoId = t.getHonorario().getProcesso().getId();
        String processoNome = t.getHonorario().getProcesso().getTituloExibicao();
        String clienteNome = t.getContraparte() != null && !t.getContraparte().isBlank() ? t.getContraparte() : "Cliente";
        usuarioProcessoRepository.findByIdProcessoIdIn(List.of(processoId)).stream()
                .map(up -> up.getUsuario())
                .filter(UsuarioEntity::isAdvogado)
                .forEach(advogado -> {
                    NotificacaoEntity n = new NotificacaoEntity();
                    n.setUsuario(advogado);
                    n.setTitulo("Pagamento para conferir");
                    n.setMensagem(clienteNome + " enviou comprovante para " + processoNome + ".");
                    n.setTipo("pagamento");
                    n.setLida(false);
                    n.setDataCriacao(LocalDateTime.now());
                    notificacaoRepository.save(n);
                });
    }

    private TransacaoEntity carregarTransacaoCliente(Jwt jwt, String idStr) {
        if (!JwtPrincipalExtractor.isCliente(jwt)) {
            throw new ForbiddenException("Área do cliente");
        }
        int tid = idStr.startsWith("cob_") ? Integer.parseInt(idStr.substring(4)) : Integer.parseInt(idStr);
        TransacaoEntity t = transacaoRepository.findById(tid).orElseThrow(() -> new NotFoundException("Cobrança não encontrada"));
        if (t.getHonorario() == null || t.getHonorario().getProcesso() == null) {
            throw new ForbiddenException("Cobrança sem processo não está disponível no portal do cliente");
        }
        processoAccessService.garantirAcessoAoProcesso(
                JwtPrincipalExtractor.userId(jwt),
                jwt,
                t.getHonorario().getProcesso().getId()
        );
        return t;
    }
}

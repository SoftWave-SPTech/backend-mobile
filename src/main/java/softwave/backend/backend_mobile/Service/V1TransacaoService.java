package softwave.backend.backend_mobile.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import softwave.backend.backend_mobile.Entity.ComprovanteEntity;
import softwave.backend.backend_mobile.Entity.HonorarioEntity;
import softwave.backend.backend_mobile.Entity.ProcessoEntity;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Exception.NotFoundException;
import softwave.backend.backend_mobile.Entity.UsuarioEntity;
import softwave.backend.backend_mobile.Repository.ComprovanteRepository;
import softwave.backend.backend_mobile.Repository.HonorarioRepository;
import softwave.backend.backend_mobile.Repository.ProcessoRepository;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;
import softwave.backend.backend_mobile.Repository.UsuarioProcessoRepository;
import softwave.backend.backend_mobile.Repository.TransacaoSpecifications;
import softwave.backend.backend_mobile.Repository.UsuarioRepository;
import softwave.backend.backend_mobile.Repository.StatusHistoricoRepository;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;
import softwave.backend.backend_mobile.Service.LocalStorageService;
import softwave.backend.backend_mobile.Service.StatusHistoricoService;
import softwave.backend.backend_mobile.util.MoneyUtil;
import softwave.backend.backend_mobile.util.TransacaoFinanceiroRules;
import softwave.backend.backend_mobile.v1.dto.TransacaoCreateRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class V1TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final HonorarioRepository honorarioRepository;
    private final ProcessoRepository processoRepository;
    private final ComprovanteRepository comprovanteRepository;
    private final ProcessoAccessService processoAccessService;
    private final LocalStorageService localStorageService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioProcessoRepository usuarioProcessoRepository;
    private final StatusHistoricoService statusHistoricoService;
    private final StatusHistoricoRepository statusHistoricoRepository;

    public V1TransacaoService(
            TransacaoRepository transacaoRepository,
            HonorarioRepository honorarioRepository,
            ProcessoRepository processoRepository,
            ComprovanteRepository comprovanteRepository,
            ProcessoAccessService processoAccessService,
            LocalStorageService localStorageService,
            UsuarioRepository usuarioRepository,
            UsuarioProcessoRepository usuarioProcessoRepository,
            StatusHistoricoService statusHistoricoService,
            StatusHistoricoRepository statusHistoricoRepository
    ) {
        this.transacaoRepository = transacaoRepository;
        this.honorarioRepository = honorarioRepository;
        this.processoRepository = processoRepository;
        this.comprovanteRepository = comprovanteRepository;
        this.processoAccessService = processoAccessService;
        this.localStorageService = localStorageService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioProcessoRepository = usuarioProcessoRepository;
        this.statusHistoricoService = statusHistoricoService;
        this.statusHistoricoRepository = statusHistoricoRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listar(
            Jwt jwt,
            String tipo,
            String status,
            LocalDate dataInicio,
            LocalDate dataFim,
            Integer periodoDias,
            int page,
            int limit
    ) {
        int uid = JwtPrincipalExtractor.userId(jwt);
        List<Integer> pids = processoAccessService.processoIdsDoUsuario(uid);
        Specification<TransacaoEntity> acessoSpec;
        if (JwtPrincipalExtractor.isAdvogado(jwt)) {
            if (pids.isEmpty()) {
                acessoSpec = TransacaoSpecifications.avulsoDoAdvogado(uid);
            } else {
                acessoSpec = TransacaoSpecifications.processoEmOuAvulso(pids, uid);
            }
        } else {
            if (pids.isEmpty()) {
                Map<String, Object> envelopeVazio = new LinkedHashMap<>();
                envelopeVazio.put("transacoes", List.of());
                envelopeVazio.put("page", 1);
                envelopeVazio.put("pageSize", 0);
                envelopeVazio.put("total", 0L);
                envelopeVazio.put("totalPages", 0);
                return envelopeVazio;
            }
            acessoSpec = TransacaoSpecifications.processoEm(pids);
        }
        LocalDate fimEfetiva = dataFim;
        LocalDate inicioEfetiva = dataInicio;
        if (periodoDias != null && periodoDias > 0 && inicioEfetiva == null && fimEfetiva == null) {
            fimEfetiva = LocalDate.now();
            inicioEfetiva = fimEfetiva.minusDays(periodoDias.longValue());
        }
        Specification<TransacaoEntity> spec = acessoSpec
                .and(TransacaoSpecifications.tipoReceitaOuDespesa(tipo))
                .and(TransacaoSpecifications.statusFinanceiroOuAprovacao(status))
                .and(TransacaoSpecifications.dataEmissaoEntre(inicioEfetiva, fimEfetiva));

        Page<TransacaoEntity> result = transacaoRepository.findAll(
                spec,
                PageRequest.of(Math.max(0, page - 1), Math.clamp(limit, 1, 100), Sort.by(Sort.Direction.DESC, "dataEmissao"))
        );
        List<Map<String, Object>> transacoes = result.getContent().stream().map(this::paraListaItem).toList();
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("transacoes", transacoes);
        envelope.put("page", result.getNumber() + 1);
        envelope.put("pageSize", result.getSize());
        envelope.put("total", result.getTotalElements());
        envelope.put("totalPages", result.getTotalPages());
        return envelope;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detalhe(Jwt jwt, String idStr) {
        TransacaoEntity t = carregarComAcesso(jwt, idStr);
        return paraDetalhe(t);
    }

    @Transactional
    public Map<String, Object> criar(Jwt jwt, TransacaoCreateRequest req) {
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Apenas advogado pode criar transação");
        }
        int uid = JwtPrincipalExtractor.userId(jwt);
        HonorarioEntity h;
        Integer hid = req.honorarioId();
        if (hid != null) {
            h = honorarioRepository.findById(hid).orElseThrow(() -> new NotFoundException("Honorário não encontrado"));
            if (h.getProcesso() == null) {
                if (!Objects.equals(h.getAdvogadoUsuarioId(), uid)) {
                    throw new ForbiddenException("Sem acesso a este honorário");
                }
            } else {
                processoAccessService.garantirAcessoAoProcesso(uid, jwt, h.getProcesso().getId());
            }
        } else if (Boolean.TRUE.equals(req.semProcesso())) {
            h = novoHonorarioAvulso(uid, req);
            honorarioRepository.save(h);
        } else if (req.processoId() != null) {
            int pid = req.processoId();
            processoAccessService.garantirAcessoAoProcesso(uid, jwt, pid);
            validarClienteVinculadoAoProcesso(req.clienteId(), pid);
            ProcessoEntity p = processoRepository.findById(pid).orElseThrow(() -> new NotFoundException("Processo não encontrado"));
            h = novoHonorarioParaProcesso(p, req);
            honorarioRepository.save(h);
        } else {
            throw new IllegalArgumentException(
                    "Informe honorarioId, processoId, ou semProcesso=true para lançamento sem processo vinculado");
        }

        int quantidadeParcelas = (req.duracaoMeses() != null && req.duracaoMeses() > 1) ? req.duracaoMeses() : 1;
        List<TransacaoEntity> criadas = criarTransacoesParceladas(h, req, quantidadeParcelas);
        if (criadas.isEmpty()) {
            throw new IllegalStateException("Falha ao criar transações parceladas.");
        }
        return Map.of(
                "id", "txn_" + criadas.get(0).getId(),
                "mensagem", quantidadeParcelas > 1
                        ? "Transações parceladas criadas com sucesso."
                        : "Transação criada com sucesso."
        );
    }

    private HonorarioEntity novoHonorarioAvulso(int advogadoUsuarioId, TransacaoCreateRequest req) {
        HonorarioEntity h = new HonorarioEntity();
        h.setProcesso(null);
        h.setAdvogadoUsuarioId(advogadoUsuarioId);
        preencherCamposComunsHonorario(h, req);
        return h;
    }

    private HonorarioEntity novoHonorarioParaProcesso(ProcessoEntity p, TransacaoCreateRequest req) {
        HonorarioEntity h = new HonorarioEntity();
        h.setProcesso(p);
        h.setAdvogadoUsuarioId(null);
        preencherCamposComunsHonorario(h, req);
        return h;
    }

    private void preencherCamposComunsHonorario(HonorarioEntity h, TransacaoCreateRequest req) {
        String baseTit = tituloPreferencial(req);
        if (baseTit.length() > 150) {
            baseTit = baseTit.substring(0, 150);
        }
        h.setTitulo(baseTit);
        if (req.valor() != null) {
            h.setValorTotal(MoneyUtil.fromCentavos(req.valor()));
        }
        h.setDataInicio(req.data() != null ? req.data() : LocalDate.now());
        h.setDataFim(req.vencimento());
        h.setStatus("pendente");
        int par = (req.duracaoMeses() != null && req.duracaoMeses() > 0) ? req.duracaoMeses() : 1;
        h.setParcelas(par);
    }

    private static String tituloPreferencial(TransacaoCreateRequest req) {
        if (req.titulo() != null && !req.titulo().isBlank()) {
            return req.titulo().trim();
        }
        if (req.descricao() != null && !req.descricao().isBlank()) {
            return req.descricao().trim();
        }
        return "Honorários";
    }

    @Transactional
    public Map<String, Object> atualizarParcial(Jwt jwt, String idStr, Map<String, Object> campos) {
        TransacaoEntity t = carregarComAcessoAdvogado(jwt, idStr);
        if (campos.containsKey("valor")) {
            Object v = campos.get("valor");
            long cent = v instanceof Number ? ((Number) v).longValue() : Long.parseLong(v.toString());
            t.setValor(MoneyUtil.fromCentavos(cent));
        }
        if (campos.containsKey("status")) {
            String novo = campos.get("status").toString();
            String anterior = t.getStatusFinanceiro();
            t.setStatusFinanceiro(novo);
            if ("pago".equalsIgnoreCase(novo)) {
                t.setDataPagamento(LocalDate.now());
            } else {
                t.setDataPagamento(null);
            }
            statusHistoricoService.registrar(t, anterior, novo, JwtPrincipalExtractor.userId(jwt), "Atualização parcial");
        }
        if (campos.containsKey("descricao")) {
            t.setDescricao(Objects.toString(campos.get("descricao"), null));
            t.setTitulo(Objects.toString(campos.get("descricao"), null));
        }
        if (campos.containsKey("vencimento")) {
            t.setDataVencimento(LocalDate.parse(campos.get("vencimento").toString()));
        }
        if (campos.containsKey("tipo")) {
            t.setTipo(Objects.toString(campos.get("tipo"), t.getTipo()));
        }
        if (campos.containsKey("categoria")) {
            t.setCategoria(Objects.toString(campos.get("categoria"), t.getCategoria()));
        }
        if (campos.containsKey("data")) {
            t.setDataEmissao(LocalDate.parse(campos.get("data").toString()));
        }
        if (campos.containsKey("clienteId")) {
            Object cidObj = campos.get("clienteId");
            if (cidObj != null) {
                String cidStr = cidObj.toString();
                if (cidStr.startsWith("cli_")) {
                    cidStr = cidStr.substring(4);
                }
                try {
                    Integer cid = Integer.parseInt(cidStr);
                    usuarioRepository.findById(cid).ifPresent(u -> t.setContraparte(u.getNome()));
                } catch (NumberFormatException ignored) {
                    // ignora cliente inválido e mantém contraparte atual.
                }
            }
        }
        transacaoRepository.save(t);
        return Map.of("mensagem", "Transação atualizada com sucesso.");
    }

    @Transactional
    public Map<String, Object> atualizarStatus(Jwt jwt, String idStr, String novoStatus) {
        TransacaoEntity t = carregarComAcessoAdvogado(jwt, idStr);
        String anterior = t.getStatusFinanceiro();
        t.setStatusFinanceiro(novoStatus);
        if ("pago".equalsIgnoreCase(novoStatus)) {
            t.setDataPagamento(LocalDate.now());
        } else {
            t.setDataPagamento(null);
        }
        transacaoRepository.save(t);
        statusHistoricoService.registrar(t, anterior, novoStatus, JwtPrincipalExtractor.userId(jwt), "Atualização de status");
        return Map.of("mensagem", "Status atualizado.", "novoStatus", novoStatus);
    }

    @Transactional
    public Map<String, Object> excluir(Jwt jwt, String idStr) {
        TransacaoEntity t = carregarComAcessoAdvogado(jwt, idStr);
        // Remove dependências explícitas para evitar violação de FK (histórico/comprovante).
        statusHistoricoRepository.deleteByTransacao_Id(t.getId());
        comprovanteRepository.deleteByTransacao_Id(t.getId());
        HonorarioEntity h = t.getHonorario();
        transacaoRepository.delete(t);
        removerHonorarioSeSemTransacoes(h);
        return Map.of("mensagem", "Transação excluída com sucesso.");
    }

    @Transactional
    public Map<String, Object> comprovante(Jwt jwt, String idStr, MultipartFile arquivo) throws IOException {
        TransacaoEntity t = carregarComAcesso(jwt, idStr);
        String path = localStorageService.salvar(arquivo, "comprovantes");
        ComprovanteEntity c = comprovanteRepository.findByTransacao_Id(t.getId()).orElse(new ComprovanteEntity());
        c.setTransacao(t);
        c.setNomeArquivo(arquivo.getOriginalFilename());
        c.setCaminhoArquivo(path);
        c.setDataUpload(LocalDateTime.now());
        comprovanteRepository.save(c);
        return Map.of(
                "mensagem", "Comprovante enviado com sucesso.",
                "comprovanteUrl", "/pagamentos/pag_" + t.getId() + "/comprovante"
        );
    }

    private void aplicarCreate(TransacaoEntity t, TransacaoCreateRequest req) {
        String tituloTx = tituloPreferencial(req);
        t.setTitulo(tituloTx.length() > 150 ? tituloTx.substring(0, 150) : tituloTx);
        if (req.valor() != null) {
            t.setValor(MoneyUtil.fromCentavos(req.valor()));
        }
        t.setTipo(req.tipo() != null ? req.tipo() : "receita");
        t.setCategoria(req.categoria());
        t.setDescricao(req.descricao() != null ? req.descricao() : req.titulo());
        t.setDataEmissao(req.data() != null ? req.data() : LocalDate.now());
        t.setDataVencimento(req.vencimento());
        t.setStatusFinanceiro(req.status() != null ? req.status() : "pendente");
        t.setStatusAprovacao("aprovado");
        if (TransacaoFinanceiroRules.estaPago(t)) {
            t.setDataPagamento(t.getDataPagamento() != null ? t.getDataPagamento() : LocalDate.now());
        } else {
            t.setDataPagamento(null);
        }
        if (req.clienteId() != null) {
            usuarioRepository.findById(req.clienteId()).ifPresent(u -> t.setContraparte(u.getNome()));
        } else if (req.contraparte() != null && !req.contraparte().isBlank()) {
            t.setContraparte(req.contraparte().trim());
        }
    }

    private List<TransacaoEntity> criarTransacoesParceladas(HonorarioEntity honorario, TransacaoCreateRequest req, int quantidadeParcelas) {
        if (req.valor() == null || req.valor() <= 0) {
            throw new IllegalArgumentException("Valor total deve ser informado para criar transação.");
        }
        long totalCentavos = req.valor();
        long base = totalCentavos / quantidadeParcelas;
        long resto = totalCentavos % quantidadeParcelas;
        LocalDate dataBase = req.data() != null ? req.data() : LocalDate.now();
        LocalDate vencimentoBase = req.vencimento() != null ? req.vencimento() : dataBase;

        List<TransacaoEntity> criadas = new ArrayList<>();
        for (int i = 0; i < quantidadeParcelas; i++) {
            long valorParcela = base + (i < resto ? 1 : 0);
            TransacaoEntity t = new TransacaoEntity();
            t.setHonorario(honorario);
            aplicarCreate(t, req);
            t.setValor(MoneyUtil.fromCentavos(valorParcela));
            t.setDataEmissao(dataBase.plusMonths(i));
            t.setDataVencimento(vencimentoBase.plusMonths(i));
            if (quantidadeParcelas > 1) {
                String tituloBase = t.getTitulo() != null && !t.getTitulo().isBlank() ? t.getTitulo() : "Parcela";
                String tituloParcela = tituloBase + " (" + (i + 1) + "/" + quantidadeParcelas + ")";
                t.setTitulo(tituloParcela.length() > 150 ? tituloParcela.substring(0, 150) : tituloParcela);
            }
            transacaoRepository.save(t);
            criadas.add(t);
        }
        return criadas;
    }

    private Map<String, Object> paraListaItem(TransacaoEntity t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "txn_" + t.getId());
        m.put("titulo", t.getTitulo());
        m.put("subtitulo", t.getContraparte());
        m.put("valor", MoneyUtil.toCentavos(MoneyUtil.toBigDecimalOrZero(t.getValor())));
        m.put("tipo", t.getTipo());
        m.put("status", t.getStatusFinanceiro());
        m.put("categoria", TransacaoFinanceiroRules.categoriaOrDefault(t));
        HonorarioEntity h = t.getHonorario();
        if (h != null && h.getProcesso() != null) {
            m.put("processoId", "proc_" + h.getProcesso().getId());
            m.put("clienteId", resolveClienteId(h.getProcesso().getId()));
        }
        m.put("data", t.getDataEmissao() != null ? t.getDataEmissao().toString() : null);
        m.put("vencimento", t.getDataVencimento() != null ? t.getDataVencimento().toString() : null);
        m.put("icone", "cash");
        return m;
    }

    private String resolveClienteId(Integer processoId) {
        return usuarioProcessoRepository.findByIdProcessoIdIn(List.of(processoId)).stream()
                .map(up -> up.getUsuario())
                .filter(UsuarioEntity::isCliente)
                .findFirst()
                .map(u -> "cli_" + u.getId())
                .orElse(null);
    }

    private Map<String, Object> paraDetalhe(TransacaoEntity t) {
        Map<String, Object> m = new LinkedHashMap<>(paraListaItem(t));
        m.put("tipo", t.getTipo());
        m.put("valor", MoneyUtil.toCentavos(MoneyUtil.toBigDecimalOrZero(t.getValor())));
        m.put("cliente", t.getContraparte());
        HonorarioEntity h = t.getHonorario();
        if (h != null && h.getProcesso() != null) {
            m.put("processo", h.getProcesso() != null ? h.getProcesso().getTituloExibicao() : null);
            m.put("processoId", "proc_" + h.getProcesso().getId());
        }
        m.put("dataPagamento", t.getDataPagamento() != null ? t.getDataPagamento().toString() : null);
        m.put("metodoPagamento", "PIX");
        m.put("observacoes", t.getObservacoes());
        m.put("comprovante", t.getComprovante() != null);
        m.put("comprovanteUrl", t.getComprovante() != null ? "/pagamentos/pag_" + t.getId() + "/comprovante" : null);
        m.put("criadoEm", LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
        m.put("atualizadoEm", LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
        return m;
    }

    private void validarClienteVinculadoAoProcesso(Integer clienteId, Integer processoId) {
        if (clienteId == null || processoId == null) {
            return;
        }
        boolean vinculado = usuarioProcessoRepository.existsByIdUsuarioIdAndIdProcessoId(clienteId, processoId);
        if (!vinculado) {
            throw new IllegalArgumentException("Cliente informado não está vinculado ao processo selecionado.");
        }
    }

    private void removerHonorarioSeSemTransacoes(HonorarioEntity honorario) {
        if (honorario == null) {
            return;
        }
        List<TransacaoEntity> restantes = transacaoRepository.findByHonorario_IdOrderByDataEmissaoAsc(honorario.getId());
        if (restantes.isEmpty()) {
            honorarioRepository.delete(honorario);
        }
    }

    private TransacaoEntity carregarComAcesso(Jwt jwt, String idStr) {
        int id = parseTxnId(idStr);
        TransacaoEntity t = transacaoRepository.findById(id).orElseThrow(() -> new NotFoundException("Transação não encontrada"));
        int uid = JwtPrincipalExtractor.userId(jwt);
        HonorarioEntity h = t.getHonorario();
        if (h.getProcesso() != null) {
            processoAccessService.garantirAcessoAoProcesso(uid, jwt, h.getProcesso().getId());
        } else {
            if (JwtPrincipalExtractor.isCliente(jwt) || !Objects.equals(h.getAdvogadoUsuarioId(), uid)) {
                throw new ForbiddenException("Sem acesso a esta transação");
            }
        }
        return t;
    }

    private TransacaoEntity carregarComAcessoAdvogado(Jwt jwt, String idStr) {
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Operação só para advogado");
        }
        return carregarComAcesso(jwt, idStr);
    }

    private static int parseTxnId(String idStr) {
        if (idStr != null && idStr.startsWith("txn_")) {
            return Integer.parseInt(idStr.substring(4));
        }
        return Integer.parseInt(idStr);
    }
}

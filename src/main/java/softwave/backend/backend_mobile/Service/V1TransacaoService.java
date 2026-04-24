package softwave.backend.backend_mobile.service;

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
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;
import softwave.backend.backend_mobile.util.MoneyUtil;
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

    public V1TransacaoService(
            TransacaoRepository transacaoRepository,
            HonorarioRepository honorarioRepository,
            ProcessoRepository processoRepository,
            ComprovanteRepository comprovanteRepository,
            ProcessoAccessService processoAccessService,
            LocalStorageService localStorageService,
            UsuarioRepository usuarioRepository,
            UsuarioProcessoRepository usuarioProcessoRepository
    ) {
        this.transacaoRepository = transacaoRepository;
        this.honorarioRepository = honorarioRepository;
        this.processoRepository = processoRepository;
        this.comprovanteRepository = comprovanteRepository;
        this.processoAccessService = processoAccessService;
        this.localStorageService = localStorageService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioProcessoRepository = usuarioProcessoRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listar(
            Jwt jwt,
            String tipo,
            String status,
            String busca,
            LocalDate dataInicio,
            LocalDate dataFim,
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
                return List.of();
            }
            acessoSpec = TransacaoSpecifications.processoEm(pids);
        }
        Specification<TransacaoEntity> spec = acessoSpec
                .and(TransacaoSpecifications.tipoReceitaOuDespesa(tipo))
                .and(TransacaoSpecifications.statusFinanceiroOuAprovacao(status))
                .and(TransacaoSpecifications.buscaTituloOuCliente(busca))
                .and(TransacaoSpecifications.dataEmissaoEntre(dataInicio, dataFim));

        Page<TransacaoEntity> result = transacaoRepository.findAll(
                spec,
                PageRequest.of(Math.max(0, page - 1), Math.max(1, Math.min(limit, 100)), Sort.by(Sort.Direction.DESC, "dataEmissao"))
        );
        return result.getContent().stream().map(this::paraListaItem).toList();
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
            ProcessoEntity p = processoRepository.findById(pid).orElseThrow(() -> new NotFoundException("Processo não encontrado"));
            h = novoHonorarioParaProcesso(p, req);
            honorarioRepository.save(h);
        } else {
            throw new IllegalArgumentException(
                    "Informe honorarioId, processoId, ou semProcesso=true para lançamento sem processo vinculado");
        }

        TransacaoEntity t = new TransacaoEntity();
        t.setHonorario(h);
        aplicarCreate(t, req);
        transacaoRepository.save(t);
        return Map.of("id", "txn_" + t.getId(), "mensagem", "Transação criada com sucesso.");
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
            t.setStatusFinanceiro(campos.get("status").toString());
        }
        if (campos.containsKey("descricao")) {
            t.setDescricao(Objects.toString(campos.get("descricao"), null));
        }
        if (campos.containsKey("vencimento")) {
            t.setDataVencimento(LocalDate.parse(campos.get("vencimento").toString()));
        }
        transacaoRepository.save(t);
        return Map.of("mensagem", "Transação atualizada com sucesso.");
    }

    @Transactional
    public Map<String, Object> atualizarStatus(Jwt jwt, String idStr, String novoStatus) {
        TransacaoEntity t = carregarComAcessoAdvogado(jwt, idStr);
        t.setStatusFinanceiro(novoStatus);
        if ("pago".equalsIgnoreCase(novoStatus)) {
            t.setDataPagamento(LocalDate.now());
        }
        transacaoRepository.save(t);
        return Map.of("mensagem", "Status atualizado.", "novoStatus", novoStatus);
    }

    @Transactional
    public Map<String, Object> excluir(Jwt jwt, String idStr) {
        TransacaoEntity t = carregarComAcessoAdvogado(jwt, idStr);
        transacaoRepository.delete(t);
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
                "comprovanteUrl", "file://" + path
        );
    }

    private void aplicarCreate(TransacaoEntity t, TransacaoCreateRequest req) {
        String tituloTx = tituloPreferencial(req);
        t.setTitulo(tituloTx.length() > 150 ? tituloTx.substring(0, 150) : tituloTx);
        if (req.valor() != null) {
            t.setValor(MoneyUtil.fromCentavos(req.valor()));
        }
        t.setTipo(req.tipo() != null ? req.tipo() : "receita");
        t.setDescricao(req.descricao() != null ? req.descricao() : req.titulo());
        t.setDataEmissao(req.data() != null ? req.data() : LocalDate.now());
        t.setDataVencimento(req.vencimento());
        t.setStatusFinanceiro(req.status() != null ? req.status() : "pendente");
        t.setStatusAprovacao("aprovado");
        if (req.clienteId() != null) {
            usuarioRepository.findById(req.clienteId()).ifPresent(u -> t.setContraparte(u.getNome()));
        } else if (req.contraparte() != null && !req.contraparte().isBlank()) {
            t.setContraparte(req.contraparte().trim());
        }
    }

    private Map<String, Object> paraListaItem(TransacaoEntity t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "txn_" + t.getId());
        m.put("titulo", t.getTitulo());
        m.put("subtitulo", t.getContraparte());
        m.put("valor", MoneyUtil.toCentavos(MoneyUtil.toBigDecimalOrZero(t.getValor())));
        m.put("tipo", t.getTipo());
        m.put("status", t.getStatusFinanceiro());
        m.put("categoria", "honorarios");
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
        m.put("comprovanteUrl", t.getComprovante() != null ? "file://" + t.getComprovante().getCaminhoArquivo() : null);
        m.put("criadoEm", LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
        m.put("atualizadoEm", LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
        return m;
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

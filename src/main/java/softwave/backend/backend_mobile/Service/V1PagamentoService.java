package softwave.backend.backend_mobile.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import softwave.backend.backend_mobile.Entity.NotificacaoEntity;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Entity.UsuarioEntity;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Exception.NotFoundException;
import softwave.backend.backend_mobile.Repository.ComprovanteRepository;
import softwave.backend.backend_mobile.Repository.NotificacaoRepository;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;
import softwave.backend.backend_mobile.Repository.UsuarioProcessoRepository;
import softwave.backend.backend_mobile.Service.ProcessoAccessService;
import softwave.backend.backend_mobile.Service.StatusHistoricoService;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;
import softwave.backend.backend_mobile.util.MoneyUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class V1PagamentoService {

    private final TransacaoRepository transacaoRepository;
    private final ComprovanteRepository comprovanteRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioProcessoRepository usuarioProcessoRepository;
    private final ProcessoAccessService processoAccessService;
    private final StatusHistoricoService statusHistoricoService;

    public V1PagamentoService(
            TransacaoRepository transacaoRepository,
            ComprovanteRepository comprovanteRepository,
            NotificacaoRepository notificacaoRepository,
            UsuarioProcessoRepository usuarioProcessoRepository,
            ProcessoAccessService processoAccessService,
            StatusHistoricoService statusHistoricoService
    ) {
        this.transacaoRepository = transacaoRepository;
        this.comprovanteRepository = comprovanteRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioProcessoRepository = usuarioProcessoRepository;
        this.processoAccessService = processoAccessService;
        this.statusHistoricoService = statusHistoricoService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> pendentes(Jwt jwt) {
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Apenas advogado");
        }
        List<Integer> pids = processoAccessService.processoIdsDoUsuario(JwtPrincipalExtractor.userId(jwt));
        List<TransacaoEntity> todas = transacaoRepository.findByHonorario_Processo_IdIn(pids);
        List<Map<String, Object>> pagamentos = new ArrayList<>();
        for (TransacaoEntity t : todas) {
            String ap = t.getStatusAprovacao();
            if (ap != null && ap.equalsIgnoreCase("pendente")) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", t.getId());
                m.put("valor", MoneyUtil.toCentavos(MoneyUtil.toBigDecimalOrZero(t.getValor())));
                m.put("data", t.getDataEmissao() != null ? t.getDataEmissao().toString() : null);
                m.put("comprovanteUrl", t.getComprovante() != null ? "/pagamentos/pag_" + t.getId() + "/comprovante" : "");
                m.put("cliente", t.getContraparte());
                m.put("processo", t.getHonorario().getProcesso() != null ? t.getHonorario().getProcesso().getTituloExibicao() : "Sem processo");
                m.put("clienteId", "cli_?");
                m.put("status", "pendente");
                pagamentos.add(m);
            }
        }
        return Map.of("total", pagamentos.size(), "pagamentos", pagamentos);
    }

    @Transactional
    public Map<String, Object> aprovar(Jwt jwt, String idStr) {
        TransacaoEntity t = carregar(jwt, idStr);
        String anterior = t.getStatusFinanceiro();
        t.setStatusAprovacao("aprovado");
        t.setStatusFinanceiro("pago");
        t.setDataPagamento(java.time.LocalDate.now());
        transacaoRepository.save(t);
        statusHistoricoService.registrar(t, anterior, "pago", JwtPrincipalExtractor.userId(jwt), "Pagamento aprovado");
        notificarClienteResultado(t, true, null);
        return Map.of("mensagem", "Pagamento aprovado com sucesso.");
    }

    @Transactional
    public Map<String, Object> reprovar(Jwt jwt, String idStr, String motivo) {
        TransacaoEntity t = carregar(jwt, idStr);
        String anterior = t.getStatusFinanceiro();
        t.setStatusAprovacao("reprovado");
        t.setStatusFinanceiro("pendente");
        t.setObservacoes(motivo);
        transacaoRepository.save(t);
        statusHistoricoService.registrar(
                t,
                anterior,
                "pendente",
                JwtPrincipalExtractor.userId(jwt),
                motivo != null ? motivo : "Pagamento reprovado"
        );
        notificarClienteResultado(t, false, motivo);
        return Map.of("mensagem", "Pagamento reprovado. Cliente será notificado.");
    }

    private void notificarClienteResultado(TransacaoEntity t, boolean aprovado, String motivo) {
        if (t.getHonorario() == null || t.getHonorario().getProcesso() == null) {
            return;
        }
        Integer processoId = t.getHonorario().getProcesso().getId();
        String processoNome = t.getHonorario().getProcesso().getTituloExibicao();
        usuarioProcessoRepository.findByIdProcessoIdIn(List.of(processoId)).stream()
                .map(up -> up.getUsuario())
                .filter(UsuarioEntity::isCliente)
                .findFirst()
                .ifPresent(cliente -> {
                    NotificacaoEntity n = new NotificacaoEntity();
                    n.setUsuario(cliente);
                    if (aprovado) {
                        n.setTitulo("Pagamento aprovado");
                        n.setMensagem("Seu comprovante do processo " + processoNome + " foi aprovado.");
                        n.setTipo("sucesso");
                    } else {
                        String msg = "Seu comprovante do processo " + processoNome + " foi reprovado.";
                        if (motivo != null && !motivo.isBlank()) {
                            msg += " Motivo: " + motivo.trim();
                        }
                        n.setTitulo("Pagamento reprovado");
                        n.setMensagem(msg);
                        n.setTipo("alerta");
                    }
                    n.setLida(false);
                    n.setDataCriacao(LocalDateTime.now());
                    notificacaoRepository.save(n);
                });
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> comprovante(Jwt jwt, String idStr) throws Exception {
        TransacaoEntity t = carregar(jwt, idStr);
        var comp = comprovanteRepository.findByTransacao_Id(t.getId())
                .orElseThrow(() -> new NotFoundException("Comprovante não encontrado"));
        Path path = Path.of(comp.getCaminhoArquivo());
        if (!Files.exists(path)) {
            throw new NotFoundException("Arquivo do comprovante não encontrado no disco");
        }
        String contentType = Files.probeContentType(path);
        MediaType mt = (contentType == null || contentType.isBlank())
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(contentType);
        return ResponseEntity.ok()
                .contentType(mt)
                .header("Content-Disposition", "inline; filename=\"" + path.getFileName() + "\"")
                .body(new PathResource(path));
    }

    private TransacaoEntity carregar(Jwt jwt, String idStr) {
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Apenas advogado");
        }
        int id = idStr.startsWith("pag_") ? Integer.parseInt(idStr.substring(4)) : Integer.parseInt(idStr);
        TransacaoEntity t = transacaoRepository.findById(id).orElseThrow(() -> new NotFoundException("Pagamento não encontrado"));
        if (t.getHonorario().getProcesso() != null) {
            processoAccessService.garantirAcessoAoProcesso(JwtPrincipalExtractor.userId(jwt), jwt, t.getHonorario().getProcesso().getId());
        }
        return t;
    }
}

package softwave.backend.backend_mobile.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Exception.NotFoundException;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;
import softwave.backend.backend_mobile.util.MoneyUtil;

import java.util.*;

@Service
public class V1PagamentoService {

    private final TransacaoRepository transacaoRepository;
    private final ProcessoAccessService processoAccessService;

    public V1PagamentoService(TransacaoRepository transacaoRepository, ProcessoAccessService processoAccessService) {
        this.transacaoRepository = transacaoRepository;
        this.processoAccessService = processoAccessService;
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
                m.put("id", "pag_" + t.getId());
                m.put("valor", MoneyUtil.toCentavos(MoneyUtil.toBigDecimalOrZero(t.getValor())));
                m.put("data", t.getDataEmissao() != null ? t.getDataEmissao().toString() : null);
                m.put("comprovanteUrl", t.getComprovante() != null ? "file://" + t.getComprovante().getCaminhoArquivo() : "");
                m.put("cliente", t.getContraparte());
                m.put("processo", t.getHonorario().getProcesso().getTituloExibicao());
                m.put("clienteId", "cli_?");
                pagamentos.add(m);
            }
        }
        return Map.of("total", pagamentos.size(), "pagamentos", pagamentos);
    }

    @Transactional
    public Map<String, Object> aprovar(Jwt jwt, String idStr) {
        TransacaoEntity t = carregar(jwt, idStr);
        t.setStatusAprovacao("aprovado");
        t.setStatusFinanceiro("pago");
        t.setDataPagamento(java.time.LocalDate.now());
        transacaoRepository.save(t);
        return Map.of("mensagem", "Pagamento aprovado com sucesso.");
    }

    @Transactional
    public Map<String, Object> reprovar(Jwt jwt, String idStr, String motivo) {
        TransacaoEntity t = carregar(jwt, idStr);
        t.setStatusAprovacao("reprovado");
        t.setObservacoes(motivo);
        transacaoRepository.save(t);
        return Map.of("mensagem", "Pagamento reprovado. Cliente será notificado.");
    }

    private TransacaoEntity carregar(Jwt jwt, String idStr) {
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Apenas advogado");
        }
        int id = idStr.startsWith("pag_") ? Integer.parseInt(idStr.substring(4)) : Integer.parseInt(idStr);
        TransacaoEntity t = transacaoRepository.findById(id).orElseThrow(() -> new NotFoundException("Pagamento não encontrado"));
        processoAccessService.garantirAcessoAoProcesso(JwtPrincipalExtractor.userId(jwt), jwt, t.getHonorario().getProcesso().getId());
        return t;
    }
}

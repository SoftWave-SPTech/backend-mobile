package softwave.backend.backend_mobile.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softwave.backend.backend_mobile.Entity.NotificacaoEntity;
import softwave.backend.backend_mobile.Repository.NotificacaoRepository;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class V1NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public V1NotificacaoService(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listarAdvogado(Jwt jwt, int page, int limit) {
        int uid = JwtPrincipalExtractor.userId(jwt);
        Page<NotificacaoEntity> p = notificacaoRepository.findByUsuario_IdOrderByDataCriacaoDesc(
                uid, PageRequest.of(Math.max(0, page - 1), Math.max(1, Math.min(limit, 50)), Sort.by(Sort.Direction.DESC, "dataCriacao"))
        );
        return paraLista(p);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listarCliente(Jwt jwt) {
        int uid = JwtPrincipalExtractor.userId(jwt);
        Page<NotificacaoEntity> p = notificacaoRepository.findByUsuario_IdOrderByDataCriacaoDesc(
                uid, PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "dataCriacao"))
        );
        return paraLista(p);
    }

    @Transactional
    public Map<String, Object> marcarLidaAdvogado(Jwt jwt, Integer id) {
        return marcarLida(jwt, id);
    }

    @Transactional
    public Map<String, Object> marcarLidaCliente(Jwt jwt, Integer id) {
        return marcarLida(jwt, id);
    }

    @Transactional
    public Map<String, Object> marcarTodasLidas(Jwt jwt) {
        int uid = JwtPrincipalExtractor.userId(jwt);
        Page<NotificacaoEntity> p = notificacaoRepository.findByUsuario_IdOrderByDataCriacaoDesc(
                uid, PageRequest.of(0, 500)
        );
        for (NotificacaoEntity n : p.getContent()) {
            n.setLida(true);
        }
        notificacaoRepository.saveAll(p.getContent());
        return Map.of("mensagem", "Todas as notificações foram marcadas como lidas.");
    }

    private Map<String, Object> marcarLida(Jwt jwt, Integer id) {
        int uid = JwtPrincipalExtractor.userId(jwt);
        NotificacaoEntity n = notificacaoRepository.findById(id).orElseThrow();
        if (!n.getUsuario().getId().equals(uid)) {
            throw new softwave.backend.backend_mobile.Exception.ForbiddenException("Notificação de outro usuário");
        }
        n.setLida(true);
        notificacaoRepository.save(n);
        return Map.of("mensagem", "Notificação marcada como lida.");
    }

    private Map<String, Object> paraLista(Page<NotificacaoEntity> p) {
        List<Map<String, Object>> list = p.getContent().stream().map(n -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", "ntf_" + n.getId());
            m.put("tipo", n.getTipo());
            m.put("titulo", n.getTitulo());
            m.put("mensagem", n.getMensagem());
            m.put("data", n.getDataCriacao() != null
                    ? n.getDataCriacao().format(DateTimeFormatter.ISO_DATE_TIME)
                    : null);
            m.put("lida", Boolean.TRUE.equals(n.getLida()));
            return m;
        }).toList();
        long naoLidas = p.getContent().stream().filter(x -> !Boolean.TRUE.equals(x.getLida())).count();
        return Map.of(
                "total", p.getTotalElements(),
                "naoLidas", naoLidas,
                "notificacoes", list
        );
    }
}

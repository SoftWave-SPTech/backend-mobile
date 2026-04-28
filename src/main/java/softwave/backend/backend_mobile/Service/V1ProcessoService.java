package softwave.backend.backend_mobile.Service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softwave.backend.backend_mobile.Entity.ProcessoEntity;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Repository.ProcessoRepository;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class V1ProcessoService {

    private final ProcessoRepository processoRepository;
    private final ProcessoAccessService processoAccessService;

    public V1ProcessoService(ProcessoRepository processoRepository, ProcessoAccessService processoAccessService) {
        this.processoRepository = processoRepository;
        this.processoAccessService = processoAccessService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listar(Jwt jwt) {
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Apenas advogado");
        }
        List<Integer> pids = processoAccessService.processoIdsDoUsuario(JwtPrincipalExtractor.userId(jwt));
        if (pids.isEmpty()) {
            return List.of();
        }
        return processoRepository.findAllById(pids).stream()
                .sorted(Comparator.comparing(ProcessoEntity::getId))
                .map(this::resumo)
                .toList();
    }

    private Map<String, Object> resumo(ProcessoEntity p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("processoId", "proc_" + p.getId());
        m.put("titulo", p.getTituloExibicao());
        return m;
    }
}

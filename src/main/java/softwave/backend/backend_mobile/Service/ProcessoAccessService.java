package softwave.backend.backend_mobile.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Repository.UsuarioProcessoRepository;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;

import java.util.List;

@Service
public class ProcessoAccessService {

    private final UsuarioProcessoRepository usuarioProcessoRepository;

    public ProcessoAccessService(UsuarioProcessoRepository usuarioProcessoRepository) {
        this.usuarioProcessoRepository = usuarioProcessoRepository;
    }

    public List<Integer> processoIdsDoUsuario(Integer usuarioId) {
        return usuarioProcessoRepository.findByIdUsuarioId(usuarioId).stream()
                .map(up -> up.getId().getProcessoId())
                .distinct()
                .toList();
    }

    public void garantirAcessoAoProcesso(Integer usuarioId, Jwt jwt, Integer processoId) {
        if (!processoIdsDoUsuario(usuarioId).contains(processoId)) {
            throw new ForbiddenException("Sem acesso a este processo");
        }
    }

    public boolean usuarioEhAdvogado(Jwt jwt) {
        return JwtPrincipalExtractor.isAdvogado(jwt);
    }

    public boolean usuarioEhCliente(Jwt jwt) {
        return JwtPrincipalExtractor.isCliente(jwt);
    }
}

package softwave.backend.backend_mobile.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softwave.backend.backend_mobile.Entity.ProcessoEntity;
import softwave.backend.backend_mobile.Entity.UsuarioEntity;
import softwave.backend.backend_mobile.Entity.UsuarioProcessoEntity;
import softwave.backend.backend_mobile.Entity.UsuarioProcessoId;
import softwave.backend.backend_mobile.Exception.BadRequestException;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Repository.ProcessoRepository;
import softwave.backend.backend_mobile.Repository.UsuarioProcessoRepository;
import softwave.backend.backend_mobile.Repository.UsuarioRepository;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;
import softwave.backend.backend_mobile.v1.dto.ClienteCreateRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class V1ClienteCadastroService {

    private static final String TIPO_CLIENTE = "usuario_fisico";

    private final UsuarioRepository usuarioRepository;
    private final UsuarioProcessoRepository usuarioProcessoRepository;
    private final ProcessoRepository processoRepository;
    private final ProcessoAccessService processoAccessService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public V1ClienteCadastroService(
            UsuarioRepository usuarioRepository,
            UsuarioProcessoRepository usuarioProcessoRepository,
            ProcessoRepository processoRepository,
            ProcessoAccessService processoAccessService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioProcessoRepository = usuarioProcessoRepository;
        this.processoRepository = processoRepository;
        this.processoAccessService = processoAccessService;
    }

    @Transactional
    public Map<String, Object> criar(Jwt jwt, ClienteCreateRequest req) {
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Apenas advogado");
        }
        if (req.nome() == null || req.nome().isBlank()) {
            throw new BadRequestException("nome é obrigatório");
        }
        String email = req.email() != null && !req.email().isBlank()
                ? req.email().trim().toLowerCase()
                : "cliente." + UUID.randomUUID() + "@cadastro.softwave.local";
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("E-mail já cadastrado");
        }
        int advId = JwtPrincipalExtractor.userId(jwt);

        UsuarioEntity u = new UsuarioEntity();
        u.setTipoUsuario(TIPO_CLIENTE);
        u.setEmail(email);
        u.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
        u.setNome(req.nome().trim());
        u.setTelefone(req.telefone() != null && !req.telefone().isBlank() ? req.telefone().trim() : null);
        LocalDateTime now = LocalDateTime.now();
        u.setCreatedAt(now);
        u.setUpdatedAt(now);
        usuarioRepository.save(u);

        List<Integer> pids = req.processoIds() != null ? req.processoIds() : List.of();
        for (Integer pid : pids) {
            if (pid == null) {
                continue;
            }
            processoAccessService.garantirAcessoAoProcesso(advId, jwt, pid);
            if (usuarioProcessoRepository.existsByIdUsuarioIdAndIdProcessoId(u.getId(), pid)) {
                continue;
            }
            ProcessoEntity proc = processoRepository.findById(pid).orElseThrow(() -> new BadRequestException("Processo inválido: " + pid));
            UsuarioProcessoEntity link = new UsuarioProcessoEntity();
            link.setId(new UsuarioProcessoId(u.getId(), pid));
            link.setUsuario(u);
            link.setProcesso(proc);
            usuarioProcessoRepository.save(link);
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "cli_" + u.getId());
        m.put("nome", u.getNome());
        m.put("email", u.getEmail());
        m.put("telefone", u.getTelefone() != null ? u.getTelefone() : "");
        m.put("mensagem", "Cliente cadastrado com sucesso.");
        return m;
    }
}

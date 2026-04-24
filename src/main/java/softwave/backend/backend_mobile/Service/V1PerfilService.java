package softwave.backend.backend_mobile.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import softwave.backend.backend_mobile.Entity.UsuarioEntity;
import softwave.backend.backend_mobile.Repository.UsuarioRepository;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class V1PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final LocalStorageService localStorageService;

    public V1PerfilService(UsuarioRepository usuarioRepository, LocalStorageService localStorageService) {
        this.usuarioRepository = usuarioRepository;
        this.localStorageService = localStorageService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> perfilAdvogado(Jwt jwt) {
        UsuarioEntity u = usuarioRepository.findById(JwtPrincipalExtractor.userId(jwt)).orElseThrow();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "usr_" + u.getId());
        m.put("nome", u.getNomeFantasia() != null ? u.getNomeFantasia() : u.getNome());
        m.put("email", u.getEmail());
        m.put("telefone", u.getTelefone());
        m.put("oab", "");
        m.put("endereco", montarEndereco(u));
        m.put("fotoPerfil", u.getFoto());
        Map<String, Object> banco = new LinkedHashMap<>();
        banco.put("banco", "—");
        banco.put("agencia", "—");
        banco.put("conta", "—");
        banco.put("favorecido", u.getRazaoSocial() != null ? u.getRazaoSocial() : u.getNome());
        banco.put("cnpj", u.getCnpj() != null ? u.getCnpj() : "");
        m.put("dadosBancarios", banco);
        return m;
    }

    @Transactional
    public Map<String, Object> atualizarAdvogado(Jwt jwt, Map<String, Object> body) {
        UsuarioEntity u = usuarioRepository.findById(JwtPrincipalExtractor.userId(jwt)).orElseThrow();
        if (body.containsKey("nome")) {
            u.setNome(body.get("nome").toString());
        }
        if (body.containsKey("telefone")) {
            u.setTelefone(body.get("telefone").toString());
        }
        if (body.containsKey("endereco")) {
            u.setLogradouro(body.get("endereco").toString());
        }
        usuarioRepository.save(u);
        return Map.of("mensagem", "Perfil atualizado com sucesso.");
    }

    @Transactional
    public Map<String, Object> fotoAdvogado(Jwt jwt, MultipartFile foto) throws IOException {
        UsuarioEntity u = usuarioRepository.findById(JwtPrincipalExtractor.userId(jwt)).orElseThrow();
        String path = localStorageService.salvar(foto, "perfil");
        u.setFoto("file://" + path);
        usuarioRepository.save(u);
        return Map.of("mensagem", "Foto atualizada com sucesso.", "fotoUrl", u.getFoto());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> perfilCliente(Jwt jwt) {
        UsuarioEntity u = usuarioRepository.findById(JwtPrincipalExtractor.userId(jwt)).orElseThrow();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "cli_" + u.getId());
        m.put("nome", u.getNome());
        m.put("email", u.getEmail());
        m.put("telefone", u.getTelefone());
        m.put("endereco", montarEndereco(u));
        m.put("cpf", u.getCpf());
        m.put("clienteDesde", u.getCreatedAt() != null ? u.getCreatedAt().toLocalDate().toString() : null);
        m.put("fotoPerfil", u.getFoto());
        m.put("processoAtivo", Map.of());
        m.put("preferencias", Map.of("notificacoesAtivas", true));
        return m;
    }

    @Transactional
    public Map<String, Object> preferenciasCliente(Jwt jwt, Map<String, Object> body) {
        return Map.of("mensagem", "Preferências atualizadas.");
    }

    @Transactional
    public Map<String, Object> fotoCliente(Jwt jwt, MultipartFile foto) throws IOException {
        return fotoAdvogado(jwt, foto);
    }

    private static String montarEndereco(UsuarioEntity u) {
        StringBuilder sb = new StringBuilder();
        if (u.getLogradouro() != null) {
            sb.append(u.getLogradouro());
        }
        if (u.getCidade() != null) {
            if (sb.length() > 0) {
                sb.append(" — ");
            }
            sb.append(u.getCidade());
        }
        return sb.toString();
    }
}

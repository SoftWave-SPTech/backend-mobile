package softwave.backend.backend_mobile.Service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import softwave.backend.backend_mobile.Entity.UsuarioEntity;
import softwave.backend.backend_mobile.Exception.NotFoundException;
import softwave.backend.backend_mobile.Repository.UsuarioRepository;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import softwave.backend.backend_mobile.Entity.NotificacaoEntity;
import softwave.backend.backend_mobile.Entity.UsuarioProcessoEntity;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Repository.NotificacaoRepository;
import softwave.backend.backend_mobile.Repository.UsuarioProcessoRepository;

@Service
public class V1PerfilService {

    private static final Logger log = LoggerFactory.getLogger(V1PerfilService.class);

    private final UsuarioRepository usuarioRepository;
    private final LocalStorageService localStorageService;
    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioProcessoRepository usuarioProcessoRepository;

    public V1PerfilService(
            UsuarioRepository usuarioRepository,
            LocalStorageService localStorageService,
            NotificacaoRepository notificacaoRepository,
            UsuarioProcessoRepository usuarioProcessoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.localStorageService = localStorageService;
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioProcessoRepository = usuarioProcessoRepository;
    }

    private UsuarioEntity carregarUsuario(Jwt jwt) {
        int userId = JwtPrincipalExtractor.userId(jwt);
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> {
                    String msg = "Usuário não encontrado no backend para id=" + userId;
                    log.warn(msg);
                    return new NotFoundException(msg);
                });
    }

    @Transactional(readOnly = true)
    public Map<String, Object> perfilAdvogado(Jwt jwt) {
        UsuarioEntity u = carregarUsuario(jwt);
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
        if (body.containsKey("nome") && body.get("nome") != null) {
            String n = body.get("nome").toString();
            u.setNome(n);
            u.setNomeFantasia(n);
        }
        if (body.containsKey("telefone") && body.get("telefone") != null) {
            u.setTelefone(body.get("telefone").toString());
        }
        if (body.containsKey("endereco") && body.get("endereco") != null) {
            u.setLogradouro(body.get("endereco").toString());
        }
        if (body.containsKey("email") && body.get("email") != null) {
            u.setEmail(body.get("email").toString().trim());
        }
        u.setUpdatedAt(LocalDateTime.now());
        usuarioRepository.save(u);
        return Map.of("mensagem", "Perfil atualizado com sucesso.");
    }

    @Transactional
    public Map<String, Object> fotoAdvogado(Jwt jwt, MultipartFile foto) throws IOException {
        UsuarioEntity u = carregarUsuario(jwt);
        String path = localStorageService.salvar(foto, "perfil");
        u.setFoto(path);
        usuarioRepository.save(u);
        return Map.of("mensagem", "Foto atualizada com sucesso.", "fotoUrl", u.getFoto());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> perfilCliente(Jwt jwt) {
        UsuarioEntity u = carregarUsuario(jwt);
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

    /**
     * Atualiza e-mail, telefone e endereço (logradouro). Nome e CPF não podem ser alterados pelo portal.
     * Notifica advogados vinculados aos mesmos processos quando houver mudança efetiva.
     */
    @Transactional
    public Map<String, Object> atualizarCliente(Jwt jwt, Map<String, Object> body) {
        if (!JwtPrincipalExtractor.isCliente(jwt)) {
            throw new ForbiddenException("Apenas clientes podem atualizar este perfil.");
        }
        UsuarioEntity u = usuarioRepository.findById(JwtPrincipalExtractor.userId(jwt)).orElseThrow();
        boolean mudou = false;
        if (body.containsKey("email") && body.get("email") != null) {
            String e = body.get("email").toString().trim();
            if (!e.equals(u.getEmail())) {
                u.setEmail(e);
                mudou = true;
            }
        }
        if (body.containsKey("telefone") && body.get("telefone") != null) {
            String t = body.get("telefone").toString();
            String atual = u.getTelefone() != null ? u.getTelefone() : "";
            if (!t.equals(atual)) {
                u.setTelefone(t);
                mudou = true;
            }
        }
        if (body.containsKey("endereco") && body.get("endereco") != null) {
            String end = body.get("endereco").toString();
            String montado = montarEndereco(u);
            if (!end.equals(montado)) {
                u.setLogradouro(end);
                mudou = true;
            }
        }
        if (mudou) {
            u.setUpdatedAt(LocalDateTime.now());
            usuarioRepository.save(u);
            notificarDonosPerfilClienteAlterado(u);
        }
        return Map.of("mensagem", "Perfil atualizado com sucesso.");
    }

    private void notificarDonosPerfilClienteAlterado(UsuarioEntity cliente) {
        List<UsuarioProcessoEntity> linksCliente = usuarioProcessoRepository.findByIdUsuarioId(cliente.getId());
        if (linksCliente.isEmpty()) {
            return;
        }
        List<Integer> processoIds = linksCliente.stream()
                .map(l -> l.getId().getProcessoId())
                .distinct()
                .collect(Collectors.toList());
        List<UsuarioProcessoEntity> todos = usuarioProcessoRepository.findByIdProcessoIdIn(processoIds);
        Set<Integer> advJaNotificado = new HashSet<>();
        String nomeCliente = cliente.getNome() != null && !cliente.getNome().isBlank()
                ? cliente.getNome()
                : "Cliente";
        for (UsuarioProcessoEntity up : todos) {
            UsuarioEntity adv = up.getUsuario();
            if (adv == null || !adv.isAdvogado()) {
                continue;
            }
            if (!advJaNotificado.add(adv.getId())) {
                continue;
            }
            NotificacaoEntity n = new NotificacaoEntity();
            n.setUsuario(adv);
            n.setTitulo("Cliente atualizou o perfil");
            n.setMensagem(nomeCliente + " alterou e-mail, telefone ou endereço no portal.");
            n.setTipo("cliente_perfil");
            n.setLida(false);
            n.setDataCriacao(LocalDateTime.now());
            notificacaoRepository.save(n);
        }
    }

    @Transactional
    public Map<String, Object> fotoCliente(Jwt jwt, MultipartFile foto) throws IOException {
        return fotoAdvogado(jwt, foto);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> fotoClienteArquivo(Jwt jwt) throws IOException {
        UsuarioEntity u = carregarUsuario(jwt);
        String foto = u.getFoto();
        if (foto == null || foto.isBlank()) {
            throw new NotFoundException("Foto de perfil não cadastrada");
        }
        String localPath = foto.startsWith("file://") ? foto.substring("file://".length()) : foto;
        Path path = Paths.get(localPath);
        if (!Files.exists(path)) {
            throw new NotFoundException("Arquivo de foto não encontrado no disco");
        }
        String contentType = Files.probeContentType(path);
        MediaType mt = (contentType == null || contentType.isBlank())
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(contentType);
        return ResponseEntity.ok()
                .contentType(mt)
                .body(new PathResource(path));
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

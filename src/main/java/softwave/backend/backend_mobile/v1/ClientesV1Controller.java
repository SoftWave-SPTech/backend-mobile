package softwave.backend.backend_mobile.v1;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import softwave.backend.backend_mobile.Entity.UsuarioEntity;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Repository.UsuarioRepository;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;
import softwave.backend.backend_mobile.Service.V1ClienteCadastroService;
import softwave.backend.backend_mobile.v1.dto.ClienteCreateRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/clientes")
public class ClientesV1Controller {

    private final UsuarioRepository usuarioRepository;
    private final V1ClienteCadastroService clienteCadastroService;

    public ClientesV1Controller(UsuarioRepository usuarioRepository, V1ClienteCadastroService clienteCadastroService) {
        this.usuarioRepository = usuarioRepository;
        this.clienteCadastroService = clienteCadastroService;
    }

    @GetMapping
    public Map<String, Object> listar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit
    ) {
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Apenas advogado");
        }
        List<UsuarioEntity> todos = usuarioRepository.findAll().stream()
                .filter(UsuarioEntity::isCliente)
                .filter(u -> busca == null || busca.isBlank()
                        || (u.getNome() != null && u.getNome().toLowerCase().contains(busca.toLowerCase())))
                .toList();
        List<Map<String, Object>> clientes = todos.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", "cli_" + u.getId());
            m.put("nome", u.getNome() != null ? u.getNome() : "");
            m.put("email", u.getEmail() != null ? u.getEmail() : "");
            m.put("telefone", u.getTelefone() != null ? u.getTelefone() : "");
            m.put("cpf", u.getCpf() != null ? u.getCpf() : "");
            m.put("clienteDesde", u.getCreatedAt() != null ? u.getCreatedAt().toLocalDate().toString() : "");
            return m;
        }).toList();
        return Map.of("total", clientes.size(), "clientes", clientes);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> criar(@AuthenticationPrincipal Jwt jwt, @RequestBody ClienteCreateRequest body) {
        return clienteCadastroService.criar(jwt, body);
    }
}

package softwave.backend.backend_mobile.v1;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import softwave.backend.backend_mobile.Service.V1PerfilService;

import java.util.Map;

@RestController
@RequestMapping("/v1/perfil")
public class PerfilV1Controller {

    private final V1PerfilService perfilService;

    public PerfilV1Controller(V1PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public Map<String, Object> get(@AuthenticationPrincipal Jwt jwt) {
        return perfilService.perfilAdvogado(jwt);
    }

    @PutMapping
    public Map<String, Object> put(@AuthenticationPrincipal Jwt jwt, @RequestBody Map<String, Object> body) {
        return perfilService.atualizarAdvogado(jwt, body);
    }

    @PostMapping("/foto")
    public Map<String, Object> foto(@AuthenticationPrincipal Jwt jwt, @RequestPart("foto") MultipartFile foto) throws Exception {
        return perfilService.fotoAdvogado(jwt, foto);
    }
}

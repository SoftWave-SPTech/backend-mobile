package softwave.backend.backend_mobile.v1;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import softwave.backend.backend_mobile.Service.V1NotificacaoService;
import softwave.backend.backend_mobile.Service.V1ClienteFluxoService;
import softwave.backend.backend_mobile.Service.V1PerfilService;

import java.util.Map;

@RestController
@RequestMapping("/v1/cliente")
public class ClienteMobileV1Controller {

    private final V1ClienteFluxoService clienteFluxoService;
    private final V1NotificacaoService notificacaoService;
    private final V1PerfilService perfilService;

    public ClienteMobileV1Controller(
            V1ClienteFluxoService clienteFluxoService,
            V1NotificacaoService notificacaoService,
            V1PerfilService perfilService
    ) {
        this.clienteFluxoService = clienteFluxoService;
        this.notificacaoService = notificacaoService;
        this.perfilService = perfilService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(@AuthenticationPrincipal Jwt jwt) {
        return clienteFluxoService.dashboard(jwt);
    }

    @GetMapping("/cobrancas")
    public Map<String, Object> cobrancas(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status
    ) {
        return clienteFluxoService.cobrancas(jwt, status);
    }

    @GetMapping("/notificacoes")
    public Map<String, Object> notificacoes(@AuthenticationPrincipal Jwt jwt) {
        return notificacaoService.listarCliente(jwt);
    }

    @PutMapping("/notificacoes/{id}/lida")
    public Map<String, Object> notifLida(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        int nid = id.startsWith("ntf_") ? Integer.parseInt(id.substring(4)) : Integer.parseInt(id);
        return notificacaoService.marcarLidaCliente(jwt, nid);
    }

    @GetMapping("/perfil")
    public Map<String, Object> perfil(@AuthenticationPrincipal Jwt jwt) {
        return perfilService.perfilCliente(jwt);
    }

    @PutMapping("/preferencias")
    public Map<String, Object> preferencias(@AuthenticationPrincipal Jwt jwt, @RequestBody Map<String, Object> body) {
        return perfilService.preferenciasCliente(jwt, body);
    }

    @PostMapping("/perfil/foto")
    public Map<String, Object> foto(@AuthenticationPrincipal Jwt jwt, @RequestPart("foto") MultipartFile foto) throws Exception {
        return perfilService.fotoCliente(jwt, foto);
    }

    @GetMapping("/perfil/foto")
    public ResponseEntity<Resource> fotoPerfilArquivo(@AuthenticationPrincipal Jwt jwt) throws Exception {
        return perfilService.fotoClienteArquivo(jwt);
    }
}

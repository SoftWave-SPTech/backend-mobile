package softwave.backend.backend_mobile.v1;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import softwave.backend.backend_mobile.service.V1ClienteFluxoService;

import java.util.Map;

@RestController
@RequestMapping("/v1")
public class CobrancaPublicaV1Controller {

    private final V1ClienteFluxoService clienteFluxoService;

    public CobrancaPublicaV1Controller(V1ClienteFluxoService clienteFluxoService) {
        this.clienteFluxoService = clienteFluxoService;
    }

    @GetMapping("/cobrancas/{id}")
    public Map<String, Object> detalhe(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        return clienteFluxoService.cobrancaDetalhe(jwt, id);
    }

    @GetMapping("/cobrancas/{id}/pix")
    public Map<String, Object> pix(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        return clienteFluxoService.pixStub(jwt, id);
    }

    @PostMapping("/cobrancas/{id}/comprovante")
    public Map<String, Object> comprovante(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @RequestPart("arquivo") MultipartFile arquivo
    ) throws Exception {
        return clienteFluxoService.comprovanteCliente(jwt, id, arquivo);
    }

    @GetMapping("/escritorio/dados-bancarios")
    public Map<String, Object> dadosBancarios() {
        return clienteFluxoService.dadosBancariosEscritorioStub();
    }
}

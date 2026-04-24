package softwave.backend.backend_mobile.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import softwave.backend.backend_mobile.DTO.UsuarioResponseDTO;
import softwave.backend.backend_mobile.DTO.UsuarioUpdateDTO;
import softwave.backend.backend_mobile.Service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscar(@PathVariable Integer id) {
        UsuarioResponseDTO response = service.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestBody UsuarioUpdateDTO dto) {

        UsuarioResponseDTO response = service.atualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<UsuarioResponseDTO> uploadFoto(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) throws Exception {

        UsuarioResponseDTO response = service.uploadFoto(id, file);
        return ResponseEntity.ok(response);
    }
}
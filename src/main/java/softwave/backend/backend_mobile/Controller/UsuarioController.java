package softwave.backend.backend_mobile.Controller;

import org.springframework.beans.factory.annotation.Autowired;
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
    public UsuarioResponseDTO buscar(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public UsuarioResponseDTO atualizar(
            @PathVariable Integer id,
            @RequestBody UsuarioUpdateDTO dto) {
        return service.atualizar(id, dto);
    }

    @PostMapping("/{id}/foto")
    public UsuarioResponseDTO uploadFoto(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) throws Exception {

        return service.uploadFoto(id, file);
    }
}
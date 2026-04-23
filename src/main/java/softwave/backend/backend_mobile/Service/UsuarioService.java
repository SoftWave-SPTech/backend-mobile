package softwave.backend.backend_mobile.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import softwave.backend.backend_mobile.Entity.*;
import softwave.backend.backend_mobile.Exception.NotFoundException;
import softwave.backend.backend_mobile.Repository.UsuarioRepository;
import softwave.backend.backend_mobile.DTO.UsuarioResponseDTO;
import softwave.backend.backend_mobile.DTO.UsuarioUpdateDTO;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private FileService fileService;

    public UsuarioResponseDTO buscarPorId(Integer id) {

        UsuarioEntity u = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        UsuarioResponseDTO dto = new UsuarioResponseDTO();

        dto.nome = u.getNome();
        dto.email = u.getEmail();
        dto.telefone = u.getTelefone();
        dto.tipoUsuario = u.getTipoUsuario();

        if (u.getFoto() != null) {
            dto.foto = "http://localhost:8080/uploads/perfis/" + u.getFoto();
        }

        dto.logradouro = u.getLogradouro();
        dto.numero = u.getNumero();
        dto.bairro = u.getBairro();
        dto.cidade = u.getCidade();
        dto.cep = u.getCep();
        dto.complemento = u.getComplemento();

        if (u instanceof ClienteEntity cliente) {
            dto.cpf = cliente.getCpf();
            dto.rg = cliente.getRg();
        }

        if (u instanceof AdvocaciaEntity adv) {
            dto.cnpj = adv.getCnpj();
            dto.nomeFantasia = adv.getNomeFantasia();
            dto.razaoSocial = adv.getRazaoSocial();
            dto.representante = adv.getRepresentante();
            dto.oab = adv.getOab();
        }

        return dto;
    }

    public UsuarioResponseDTO atualizar(Integer id, UsuarioUpdateDTO dto) {

        UsuarioEntity u = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        u.setNome(dto.nome);
        u.setEmail(dto.email);
        u.setTelefone(dto.telefone);
        u.setTipoUsuario(dto.tipoUsuario);

        u.setLogradouro(dto.logradouro);
        u.setNumero(dto.numero);
        u.setBairro(dto.bairro);
        u.setCidade(dto.cidade);
        u.setCep(dto.cep);
        u.setComplemento(dto.complemento);

        if (u instanceof ClienteEntity cliente) {
            cliente.setCpf(dto.cpf);
            cliente.setRg(dto.rg);
        }

        if (u instanceof AdvocaciaEntity adv) {
            adv.setCnpj(dto.cnpj);
            adv.setNomeFantasia(dto.nomeFantasia);
            adv.setRazaoSocial(dto.razaoSocial);
            adv.setRepresentante(dto.representante);
            adv.setOab(dto.oab);
        }

        repository.save(u);

        return buscarPorId(id);
    }

    public UsuarioResponseDTO uploadFoto(Integer id, MultipartFile file) throws Exception {

        UsuarioEntity u = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        String nomeArquivo = fileService.salvarImagem(file);

        u.setFoto(nomeArquivo);

        repository.save(u);

        return buscarPorId(id);
    }
}
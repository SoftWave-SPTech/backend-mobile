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

        // 🔹 CAMPOS GERAIS
        if (dto.nome != null) {
            u.setNome(dto.nome);
        }

        if (dto.email != null) {
            u.setEmail(dto.email);
        }

        if (dto.telefone != null) {
            u.setTelefone(dto.telefone);
        }

        if (dto.logradouro != null) {
            u.setLogradouro(dto.logradouro);
        }

        if (dto.numero != null) {
            u.setNumero(dto.numero);
        }

        if (dto.bairro != null) {
            u.setBairro(dto.bairro);
        }

        if (dto.cidade != null) {
            u.setCidade(dto.cidade);
        }

        if (dto.cep != null) {
            u.setCep(dto.cep);
        }

        if (dto.complemento != null) {
            u.setComplemento(dto.complemento);
        }

        // 🔹 CLIENTE
        if (u instanceof ClienteEntity cliente) {

            if (dto.cpf != null) {
                cliente.setCpf(dto.cpf);
            }

            if (dto.rg != null) {
                cliente.setRg(dto.rg);
            }
        }

        // 🔹 ADVOCACIA
        if (u instanceof AdvocaciaEntity adv) {

            if (dto.cnpj != null) {
                adv.setCnpj(dto.cnpj);
            }

            if (dto.nomeFantasia != null) {
                adv.setNomeFantasia(dto.nomeFantasia);
            }

            if (dto.razaoSocial != null) {
                adv.setRazaoSocial(dto.razaoSocial);
            }

            if (dto.representante != null) {
                adv.setRepresentante(dto.representante);
            }

            if (dto.oab != null) {
                adv.setOab(dto.oab);
            }
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
package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class ClienteEntity extends UsuarioEntity {
    private String cpf;

    public ClienteEntity() {}

    public ClienteEntity(String cpf) {
        this.cpf = cpf;
    }

    public ClienteEntity(
            Integer id,
            String nome,
            String email,
            LocalDate dataCadastro,
            String fotoPerfil,
            String senhaHash,
            String endereco,
            String telefone,
            String cpf
    ) {
        super(id, nome, email, dataCadastro, fotoPerfil, senhaHash, endereco, telefone);
        this.cpf = cpf;
    }

    public ClienteEntity(
            String nome,
            String email,
            String telefone,
            String endereco,
            String fotoPerfil,
            String senhaHash,
            LocalDate dataCadastro,
            String cpf
    ) {
        super(nome, email, telefone, endereco, fotoPerfil, senhaHash, dataCadastro);
        this.cpf = cpf;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}

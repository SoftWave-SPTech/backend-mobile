package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class AdvocaciaEntity extends UsuarioEntity {

    private String nomeEscritorio;
    private String oab;

    public AdvocaciaEntity() {}

    public AdvocaciaEntity(
            String nomeEscritorio,
            String oab
    ) {
        this.nomeEscritorio = nomeEscritorio;
        this.oab = oab;
    }

    public AdvocaciaEntity(
            Integer id, String nome,
            String email,
            LocalDate dataCadastro,
            String fotoPerfil,
            String senhaHash,
            String endereco,
            String telefone,
            String nomeEscritorio,
            String oab
    ) {
        super(id, nome, email, dataCadastro, fotoPerfil, senhaHash, endereco, telefone);
        this.nomeEscritorio = nomeEscritorio;
        this.oab = oab;
    }

    public AdvocaciaEntity(
            String nome,
            String email,
            String telefone,
            String endereco,
            String fotoPerfil,
            String senhaHash,
            LocalDate dataCadastro,
            String nomeEscritorio,
            String oab
    ) {
        super(nome, email, telefone, endereco, fotoPerfil, senhaHash, dataCadastro);
        this.nomeEscritorio = nomeEscritorio;
        this.oab = oab;
    }

    public String getNomeEscritorio() {
        return nomeEscritorio;
    }

    public void setNomeEscritorio(String nomeEscritorio) {
        this.nomeEscritorio = nomeEscritorio;
    }

    public String getOab() {
        return oab;
    }

    public void setOab(String oab) {
        this.oab = oab;
    }
}

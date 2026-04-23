package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("cliente")
public class ClienteEntity extends UsuarioEntity {

    @Column(unique = true)
    private String cpf;

    @Column(unique = true)
    private String rg;

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
}
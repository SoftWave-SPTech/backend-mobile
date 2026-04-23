package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("advocacia")
public class AdvocaciaEntity extends UsuarioEntity {

    @Column(unique = true)
    private String cnpj;

    @Column(name = "nome_fantasia")
    private String nomeFantasia;

    @Column(name = "razao_social")
    private String razaoSocial;

    private String representante;

    @Column(unique = true)
    private String oab;

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getRepresentante() { return representante; }
    public void setRepresentante(String representante) { this.representante = representante; }

    public String getOab() { return oab; }
    public void setOab(String oab) { this.oab = oab; }
}
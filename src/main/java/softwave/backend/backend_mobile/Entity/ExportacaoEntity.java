package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class ExportacaoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String tipoxportacao;
    private String formato;
    private LocalDate dataGeracao;
    private String arquivoGerado;

    public ExportacaoEntity() {
    }

    public ExportacaoEntity(
            Integer id,
            String tipoxportacao,
            String formato,
            LocalDate dataGeracao,
            String arquivoGerado
    ) {
        this.id = id;
        this.tipoxportacao = tipoxportacao;
        this.formato = formato;
        this.dataGeracao = dataGeracao;
        this.arquivoGerado = arquivoGerado;
    }

    public ExportacaoEntity(
            String tipoxportacao,
            String formato,
            LocalDate dataGeracao,
            String arquivoGerado
    ) {
        this.tipoxportacao = tipoxportacao;
        this.formato = formato;
        this.dataGeracao = dataGeracao;
        this.arquivoGerado = arquivoGerado;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipoxportacao() {
        return tipoxportacao;
    }

    public void setTipoxportacao(String tipoxportacao) {
        this.tipoxportacao = tipoxportacao;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public LocalDate getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDate dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

    public String getArquivoGerado() {
        return arquivoGerado;
    }

    public void setArquivoGerado(String arquivoGerado) {
        this.arquivoGerado = arquivoGerado;
    }
}

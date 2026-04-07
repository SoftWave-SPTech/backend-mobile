package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class InsightIAEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String tipoAnalise;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String resultadoTexto;
    private LocalDate dataGeracao;

    public InsightIAEntity() {
    }

    public InsightIAEntity(
            Integer id,
            LocalDate dataInicio,
            String resultadoTexto,
            LocalDate dataGeracao,
            LocalDate dataFim,
            String tipoAnalise
    ) {
        this.id = id;
        this.dataInicio = dataInicio;
        this.resultadoTexto = resultadoTexto;
        this.dataGeracao = dataGeracao;
        this.dataFim = dataFim;
        this.tipoAnalise = tipoAnalise;
    }

    public InsightIAEntity(
            String tipoAnalise,
            LocalDate dataInicio,
            String resultadoTexto,
            LocalDate dataGeracao,
            LocalDate dataFim
    ) {
        this.tipoAnalise = tipoAnalise;
        this.dataInicio = dataInicio;
        this.resultadoTexto = resultadoTexto;
        this.dataGeracao = dataGeracao;
        this.dataFim = dataFim;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipoAnalise() {
        return tipoAnalise;
    }

    public void setTipoAnalise(String tipoAnalise) {
        this.tipoAnalise = tipoAnalise;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public String getResultadoTexto() {
        return resultadoTexto;
    }

    public void setResultadoTexto(String resultadoTexto) {
        this.resultadoTexto = resultadoTexto;
    }

    public LocalDate getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDate dataGeracao) {
        this.dataGeracao = dataGeracao;
    }
}

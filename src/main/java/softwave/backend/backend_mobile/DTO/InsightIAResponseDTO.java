package softwave.backend.backend_mobile.DTO;

import softwave.backend.backend_mobile.Entity.InsightIAEntity;

import java.time.LocalDate;

public class InsightIAResponseDTO {

    private Integer id;
    private String tipoAnalise;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String resultadoTexto;
    private LocalDate dataGeracao;

    public InsightIAResponseDTO(InsightIAEntity entity) {
        this.id = entity.getId();
        this.tipoAnalise = entity.getTipoAnalise();
        this.dataInicio = entity.getDataInicio();
        this.dataFim = entity.getDataFim();
        this.resultadoTexto = entity.getResultadoTexto();
        this.dataGeracao = entity.getDataGeracao();
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

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
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

    // getters
}
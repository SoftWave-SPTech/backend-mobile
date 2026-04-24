package softwave.backend.backend_mobile.DTO;

import softwave.backend.backend_mobile.Entity.HonorarioEntity;

import java.time.LocalDate;

public class HonorarioResponseDTO {

    private Integer id;
    private String titulo;
    private Double valorTotal;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String status;
    private Integer parcelas;

    public HonorarioResponseDTO(HonorarioEntity entity) {
        this.id = entity.getId();
        this.titulo = entity.getTitulo();
        this.valorTotal = entity.getValorTotal();
        this.dataInicio = entity.getDataInicio();
        this.dataFim = entity.getDataFim();
        this.status = entity.getStatus();
        this.parcelas = entity.getParcelas();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getParcelas() {
        return parcelas;
    }

    public void setParcelas(Integer parcelas) {
        this.parcelas = parcelas;
    }

    // getters
}
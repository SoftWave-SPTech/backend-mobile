package softwave.backend.backend_mobile.DTO;

import softwave.backend.backend_mobile.Entity.ProcessoEntity;

import java.time.LocalDate;

public class ProcessoResponseDTO {

    private Integer id;
    private String numeroProcesso;
    private String titulo;
    private String descricao;
    private String status;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String categoria;
    private Integer clienteId;

    public ProcessoResponseDTO(ProcessoEntity entity) {
        this.id = entity.getId();
        this.numeroProcesso = entity.getNumeroProcesso();
        this.titulo = entity.getTitulo();
        this.descricao = entity.getDescricao();
        this.status = entity.getStatus();
        this.dataInicio = entity.getDataInicio();
        this.dataFim = entity.getDataFim();
        this.categoria = entity.getCategoria();
        this.clienteId = entity.getCliente() != null ? entity.getCliente().getId() : null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    // getters
}
package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class ProcessoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String numeroProcesso;
    private String titulo;
    private String descricao;
    private String status;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String categoria;

    public ProcessoEntity() {
    }

    public ProcessoEntity(
            Integer id,
            String numeroProcesso,
            String titulo,
            String status,
            String descricao,
            LocalDate dataInicio,
            LocalDate dataFim,
            String categoria
    ) {
        this.id = id;
        this.numeroProcesso = numeroProcesso;
        this.titulo = titulo;
        this.status = status;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.categoria = categoria;
    }

    public ProcessoEntity(
            String numeroProcesso,
            String titulo,
            String descricao,
            String status,
            LocalDate dataInicio,
            LocalDate dataFim,
            String categoria
    ) {
        this.numeroProcesso = numeroProcesso;
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = status;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.categoria = categoria;
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
}

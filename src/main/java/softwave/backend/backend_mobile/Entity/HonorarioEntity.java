package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class HonorarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String titulo;
    private Double valorTotal;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String status;
    private Integer parcelas;

    @OneToMany(mappedBy = "honorario")
    private List<TransacaoEntity> transacoes;

    @ManyToOne
    @JoinColumn(name = "processo_id")
    private ProcessoEntity processo;

    public HonorarioEntity() {
    }

    public HonorarioEntity(
            Integer id,
            String titulo,
            Double valorTotal,
            LocalDate dataInicio,
            LocalDate dataFim,
            String status,
            Integer parcelas
    ) {
        this.id = id;
        this.titulo = titulo;
        this.valorTotal = valorTotal;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
        this.parcelas = parcelas;
    }

    public HonorarioEntity(
            String titulo,
            Double valorTotal,
            LocalDate dataInicio,
            LocalDate dataFim,
            String status,
            Integer parcelas
    ) {
        this.titulo = titulo;
        this.valorTotal = valorTotal;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
        this.parcelas = parcelas;
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

    public Integer getParcelas() {
        return parcelas;
    }

    public void setParcelas(Integer parcelas) {
        this.parcelas = parcelas;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TransacaoEntity> getTransacoes() {
        return transacoes;
    }

    public void setTransacoes(List<TransacaoEntity> transacoes) {
        this.transacoes = transacoes;
    }

    public ProcessoEntity getProcesso() {
        return processo;
    }

    public void setProcesso(ProcessoEntity processo) {
        this.processo = processo;
    }
}

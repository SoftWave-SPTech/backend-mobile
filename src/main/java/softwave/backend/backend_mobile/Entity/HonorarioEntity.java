package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "honorario")
public class HonorarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Opcional: honorários avulsos (sem processo) usam {@link #advogadoUsuarioId} para controle de acesso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id", nullable = true)
    private ProcessoEntity processo;

    /** Advogado que criou honorário sem processo; ignorado quando há {@link #processo}. */
    @Column(name = "advogado_usuario_id")
    private Integer advogadoUsuarioId;

    @Column(length = 150)
    private String titulo;

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(length = 50)
    private String status;

    private Integer parcelas;

    @OneToMany(mappedBy = "honorario", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<TransacaoEntity> transacoes = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ProcessoEntity getProcesso() {
        return processo;
    }

    public void setProcesso(ProcessoEntity processo) {
        this.processo = processo;
    }

    public Integer getAdvogadoUsuarioId() {
        return advogadoUsuarioId;
    }

    public void setAdvogadoUsuarioId(Integer advogadoUsuarioId) {
        this.advogadoUsuarioId = advogadoUsuarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
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

    public List<TransacaoEntity> getTransacoes() {
        return transacoes;
    }

    public void setTransacoes(List<TransacaoEntity> transacoes) {
        this.transacoes = transacoes;
    }
}

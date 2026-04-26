package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "status_historico")
public class StatusHistoricoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transacao_id", nullable = false)
    private TransacaoEntity transacao;

    @Column(name = "status_anterior", length = 50)
    private String statusAnterior;

    @Column(name = "status_novo", length = 50, nullable = false)
    private String statusNovo;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TransacaoEntity getTransacao() {
        return transacao;
    }

    public void setTransacao(TransacaoEntity transacao) {
        this.transacao = transacao;
    }

    public String getStatusAnterior() {
        return statusAnterior;
    }

    public void setStatusAnterior(String statusAnterior) {
        this.statusAnterior = statusAnterior;
    }

    public String getStatusNovo() {
        return statusNovo;
    }

    public void setStatusNovo(String statusNovo) {
        this.statusNovo = statusNovo;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}


package softwave.backend.backend_mobile.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class TransacaoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String titulo;
    private Double valor;
    private String tipo;
    private StatusFinanceiro statusFinanceiro;
    private StatusAprovacao statusAprovacao;
    private LocalDate dataEmissao;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private String descricao;
    private String observacoes;
    private String contraparte;

    @ManyToOne
    @JoinColumn(name = "honorario_id")
    private HonorarioEntity honorario;

    @OneToMany(mappedBy = "transacao")
    @JsonIgnore
    private List<ComprovanteEntity> comprovantes;

    public TransacaoEntity() {
    }

    public TransacaoEntity(
            Integer id,
            String titulo,
            Double valor,
            String tipo,
            StatusFinanceiro statusFinanceiro,
            StatusAprovacao statusAprovacao,
            LocalDate dataEmissao,
            LocalDate dataVencimento,
            LocalDate dataPagamento,
            String observacoes,
            String descricao,
            String contraparte
    ) {
        this.id = id;
        this.titulo = titulo;
        this.valor = valor;
        this.tipo = tipo;
        this.statusFinanceiro = statusFinanceiro;
        this.statusAprovacao = statusAprovacao;
        this.dataEmissao = dataEmissao;
        this.dataVencimento = dataVencimento;
        this.dataPagamento = dataPagamento;
        this.observacoes = observacoes;
        this.descricao = descricao;
        this.contraparte = contraparte;
    }

    public TransacaoEntity(
            String titulo,
            Double valor,
            String tipo,
            StatusFinanceiro statusFinanceiro,
            StatusAprovacao statusAprovacao,
            LocalDate dataEmissao,
            LocalDate dataVencimento,
            LocalDate dataPagamento,
            String descricao,
            String observacoes,
            String contraparte
    ) {
        this.titulo = titulo;
        this.valor = valor;
        this.tipo = tipo;
        this.statusFinanceiro = statusFinanceiro;
        this.statusAprovacao = statusAprovacao;
        this.dataEmissao = dataEmissao;
        this.dataVencimento = dataVencimento;
        this.dataPagamento = dataPagamento;
        this.descricao = descricao;
        this.observacoes = observacoes;
        this.contraparte = contraparte;
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

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public StatusAprovacao getStatusAprovacao() {
        return statusAprovacao;
    }

    public void setStatusAprovacao(StatusAprovacao statusAprovacao) {
        this.statusAprovacao = statusAprovacao;
    }

    public StatusFinanceiro getStatusFinanceiro() {
        return statusFinanceiro;
    }

    public void setStatusFinanceiro(StatusFinanceiro statusFinanceiro) {
        this.statusFinanceiro = statusFinanceiro;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getContraparte() {
        return contraparte;
    }

    public void setContraparte(String contraparte) {
        this.contraparte = contraparte;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public HonorarioEntity getHonorario() {
        return honorario;
    }

    public void setHonorario(HonorarioEntity honorario) {
        this.honorario = honorario;
    }

    public List<ComprovanteEntity> getComprovantes() {
        return comprovantes;
    }

    public void setComprovantes(List<ComprovanteEntity> comprovantes) {
        this.comprovantes = comprovantes;
    }
}

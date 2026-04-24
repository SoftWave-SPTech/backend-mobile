package softwave.backend.backend_mobile.DTO;

import softwave.backend.backend_mobile.Entity.TransacaoEntity;

import java.time.LocalDate;

public class TransacaoResponseDTO {

    private Integer id;
    private String titulo;
    private Double valor;
    private String tipo;
    private String statusFinanceiro;
    private String statusAprovacao;
    private LocalDate dataEmissao;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private String descricao;
    private String observacoes;
    private String contraparte;

    public TransacaoResponseDTO(TransacaoEntity entity) {
        this.id = entity.getId();
        this.titulo = entity.getTitulo();
        this.valor = entity.getValor();
        this.tipo = entity.getTipo();
        this.statusFinanceiro = String.valueOf(entity.getStatusFinanceiro());
        this.statusAprovacao = String.valueOf(entity.getStatusAprovacao());
        this.dataEmissao = entity.getDataEmissao();
        this.dataVencimento = entity.getDataVencimento();
        this.dataPagamento = entity.getDataPagamento();
        this.descricao = entity.getDescricao();
        this.observacoes = entity.getObservacoes();
        this.contraparte = entity.getContraparte();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getStatusAprovacao() {
        return statusAprovacao;
    }

    public void setStatusAprovacao(String statusAprovacao) {
        this.statusAprovacao = statusAprovacao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public String getStatusFinanceiro() {
        return statusFinanceiro;
    }

    public void setStatusFinanceiro(String statusFinanceiro) {
        this.statusFinanceiro = statusFinanceiro;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // getters
}

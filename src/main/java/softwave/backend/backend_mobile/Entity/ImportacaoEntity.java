package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class ImportacaoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nomeArquivo;
    private String tipoArquivo;
    private String tipoImportacao;
    private String bancoOrigem;
    private LocalDate dataUpload;
    private String status;
    private Integer totalRegistros;
    private Integer registrosNovos;
    private Integer registrosAtualizados;
    private Integer registroErro;
    private String mensagemErro;

    public ImportacaoEntity() {
    }

    public ImportacaoEntity(
            Integer id,
            String nomeArquivo,
            String tipoArquivo,
            String tipoImportacao,
            String bancoOrigem,
            LocalDate dataUpload,
            String status,
            Integer totalRegistros,
            Integer registrosNovos,
            Integer registrosAtualizados,
            Integer registroErro,
            String mensagemErro
    ) {
        this.id = id;
        this.nomeArquivo = nomeArquivo;
        this.tipoArquivo = tipoArquivo;
        this.tipoImportacao = tipoImportacao;
        this.bancoOrigem = bancoOrigem;
        this.dataUpload = dataUpload;
        this.status = status;
        this.totalRegistros = totalRegistros;
        this.registrosNovos = registrosNovos;
        this.registrosAtualizados = registrosAtualizados;
        this.registroErro = registroErro;
        this.mensagemErro = mensagemErro;
    }

    public ImportacaoEntity(
            String nomeArquivo,
            String tipoArquivo,
            String tipoImportacao,
            String bancoOrigem,
            LocalDate dataUpload,
            String status,
            Integer totalRegistros,
            Integer registrosNovos,
            Integer registrosAtualizados,
            Integer registroErro,
            String mensagemErro
    ) {
        this.nomeArquivo = nomeArquivo;
        this.tipoArquivo = tipoArquivo;
        this.tipoImportacao = tipoImportacao;
        this.bancoOrigem = bancoOrigem;
        this.dataUpload = dataUpload;
        this.status = status;
        this.totalRegistros = totalRegistros;
        this.registrosNovos = registrosNovos;
        this.registrosAtualizados = registrosAtualizados;
        this.registroErro = registroErro;
        this.mensagemErro = mensagemErro;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public String getTipoArquivo() {
        return tipoArquivo;
    }

    public void setTipoArquivo(String tipoArquivo) {
        this.tipoArquivo = tipoArquivo;
    }

    public String getTipoImportacao() {
        return tipoImportacao;
    }

    public void setTipoImportacao(String tipoImportacao) {
        this.tipoImportacao = tipoImportacao;
    }

    public String getBancoOrigem() {
        return bancoOrigem;
    }

    public void setBancoOrigem(String bancoOrigem) {
        this.bancoOrigem = bancoOrigem;
    }

    public LocalDate getDataUpload() {
        return dataUpload;
    }

    public void setDataUpload(LocalDate dataUpload) {
        this.dataUpload = dataUpload;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(Integer totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    public Integer getRegistrosAtualizados() {
        return registrosAtualizados;
    }

    public void setRegistrosAtualizados(Integer registrosAtualizados) {
        this.registrosAtualizados = registrosAtualizados;
    }

    public Integer getRegistroErro() {
        return registroErro;
    }

    public void setRegistroErro(Integer registroErro) {
        this.registroErro = registroErro;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public void setMensagemErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
    }

    public Integer getRegistrosNovos() {
        return registrosNovos;
    }

    public void setRegistrosNovos(Integer registrosNovos) {
        this.registrosNovos = registrosNovos;
    }
}
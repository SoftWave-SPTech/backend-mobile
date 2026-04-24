package softwave.backend.backend_mobile.DTO;

import softwave.backend.backend_mobile.Entity.ComprovanteEntity;

import java.time.LocalDate;

public class ComprovanteResponseDTO {

    private Integer id;
    private String nomeArquivo;
    private String caminhoArquivo;
    private LocalDate dataUpload;
    private Integer transacaoId;

    public ComprovanteResponseDTO(ComprovanteEntity entity) {
        this.id = entity.getId();
        this.nomeArquivo = entity.getNomeArquivo();
        this.caminhoArquivo = entity.getCaminhoArquivo();
        this.dataUpload = entity.getDataUpload();
        this.transacaoId = entity.getTransacao() != null
                ? entity.getTransacao().getId()
                : null;
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

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    public LocalDate getDataUpload() {
        return dataUpload;
    }

    public void setDataUpload(LocalDate dataUpload) {
        this.dataUpload = dataUpload;
    }

    public Integer getTransacaoId() {
        return transacaoId;
    }

    public void setTransacaoId(Integer transacaoId) {
        this.transacaoId = transacaoId;
    }

    // getters
}
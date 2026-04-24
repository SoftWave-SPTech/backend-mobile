package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class ComprovanteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nomeArquivo;
    private String caminhoArquivo;
    private LocalDate dataUpload;
    private String driveFileId;

    @ManyToOne
    @JoinColumn(name = "transacao_id")
    private TransacaoEntity transacao;

    public ComprovanteEntity() {
    }

    public ComprovanteEntity(
            Integer id,
            String nomeArquivo,
            String caminhoArquivo,
            LocalDate dataUpload
    ) {
        this.id = id;
        this.nomeArquivo = nomeArquivo;
        this.caminhoArquivo = caminhoArquivo;
        this.dataUpload = dataUpload;
    }

    public ComprovanteEntity(
            String nomeArquivo,
            String caminhoArquivo,
            LocalDate dataUpload
    ) {
        this.nomeArquivo = nomeArquivo;
        this.caminhoArquivo = caminhoArquivo;
        this.dataUpload = dataUpload;
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

    public TransacaoEntity getTransacao() {
        return transacao;
    }

    public void setTransacao(TransacaoEntity transacao) {
        this.transacao = transacao;
    }

    public String getDriveFileId() {
        return driveFileId;
    }

    public void setDriveFileId(String driveFileId) {
        this.driveFileId = driveFileId;
    }
}

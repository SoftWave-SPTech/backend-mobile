package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comprovante")
public class ComprovanteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transacao_id", nullable = false, unique = true)
    private TransacaoEntity transacao;

    @Column(name = "nome_arquivo")
    private String nomeArquivo;

    @Column(name = "caminho_arquivo")
    private String caminhoArquivo;

    @Column(name = "data_upload")
    private LocalDateTime dataUpload;

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

    public LocalDateTime getDataUpload() {
        return dataUpload;
    }

    public void setDataUpload(LocalDateTime dataUpload) {
        this.dataUpload = dataUpload;
    }
}

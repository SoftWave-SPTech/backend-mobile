package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class NotificacaoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String titulo;
    private String mensagem;
    private String  tipo;
    private Boolean lida;
    private LocalDate dataCriacao;

    public NotificacaoEntity() {
    }

    public NotificacaoEntity(
            Integer id,
            String mensagem,
            String titulo,
            Boolean lida,
            String tipo,
            LocalDate dataCriacao
    ) {
        this.id = id;
        this.mensagem = mensagem;
        this.titulo = titulo;
        this.lida = lida;
        this.tipo = tipo;
        this.dataCriacao = dataCriacao;
    }

    public NotificacaoEntity(
            String titulo,
            String tipo,
            String mensagem,
            Boolean lida,
            LocalDate dataCriacao
    ) {
        this.titulo = titulo;
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.lida = lida;
        this.dataCriacao = dataCriacao;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Boolean getLida() {
        return lida;
    }

    public void setLida(Boolean lida) {
        this.lida = lida;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}

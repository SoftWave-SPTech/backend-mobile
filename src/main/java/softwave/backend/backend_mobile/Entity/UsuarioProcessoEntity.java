package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios_processos")
public class UsuarioProcessoEntity {

    @EmbeddedId
    private UsuarioProcessoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("processoId")
    @JoinColumn(name = "processo_id")
    private ProcessoEntity processo;

    public UsuarioProcessoId getId() {
        return id;
    }

    public void setId(UsuarioProcessoId id) {
        this.id = id;
    }

    public UsuarioEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }

    public ProcessoEntity getProcesso() {
        return processo;
    }

    public void setProcesso(ProcessoEntity processo) {
        this.processo = processo;
    }
}

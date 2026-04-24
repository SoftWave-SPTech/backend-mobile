package softwave.backend.backend_mobile.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UsuarioProcessoId implements Serializable {

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "processo_id")
    private Integer processoId;

    public UsuarioProcessoId() {}

    public UsuarioProcessoId(Integer usuarioId, Integer processoId) {
        this.usuarioId = usuarioId;
        this.processoId = processoId;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Integer getProcessoId() {
        return processoId;
    }

    public void setProcessoId(Integer processoId) {
        this.processoId = processoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioProcessoId that = (UsuarioProcessoId) o;
        return Objects.equals(usuarioId, that.usuarioId) && Objects.equals(processoId, that.processoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, processoId);
    }
}

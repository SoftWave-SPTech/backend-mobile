package softwave.backend.backend_mobile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softwave.backend.backend_mobile.Entity.UsuarioProcessoEntity;
import softwave.backend.backend_mobile.Entity.UsuarioProcessoId;

import java.util.Collection;
import java.util.List;

@Repository
public interface UsuarioProcessoRepository extends JpaRepository<UsuarioProcessoEntity, UsuarioProcessoId> {

    List<UsuarioProcessoEntity> findByIdUsuarioId(Integer usuarioId);

    List<UsuarioProcessoEntity> findByIdProcessoIdIn(Collection<Integer> processoIds);

    boolean existsByIdUsuarioIdAndIdProcessoId(Integer usuarioId, Integer processoId);
}

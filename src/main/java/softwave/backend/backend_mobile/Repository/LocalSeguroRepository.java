package softwave.backend.backend_mobile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softwave.backend.backend_mobile.Entity.LocalSeguroEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalSeguroRepository extends JpaRepository<LocalSeguroEntity, Integer> {

    List<LocalSeguroEntity> findByUsuario_IdOrderByCreatedAtDesc(Integer usuarioId);

    Optional<LocalSeguroEntity> findByIdAndUsuario_Id(Integer id, Integer usuarioId);
}

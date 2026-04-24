package softwave.backend.backend_mobile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softwave.backend.backend_mobile.Entity.HonorarioEntity;

import java.util.List;

@Repository
public interface HonorarioRepository extends JpaRepository<HonorarioEntity, Integer> {

    List<HonorarioEntity> findByProcessoId(Integer processoId);
}

package softwave.backend.backend_mobile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softwave.backend.backend_mobile.Entity.ProcessoEntity;

@Repository
public interface ProcessoRepository extends JpaRepository<ProcessoEntity, Integer> {
}

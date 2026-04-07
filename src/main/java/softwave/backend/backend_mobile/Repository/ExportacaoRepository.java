package softwave.backend.backend_mobile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softwave.backend.backend_mobile.Entity.ExportacaoEntity;

@Repository
public interface ExportacaoRepository extends JpaRepository<ExportacaoEntity, Integer> {
}

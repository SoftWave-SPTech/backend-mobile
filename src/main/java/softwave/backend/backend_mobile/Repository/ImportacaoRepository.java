package softwave.backend.backend_mobile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softwave.backend.backend_mobile.Entity.ImportacaoEntity;

@Repository
public interface ImportacaoRepository extends JpaRepository<ImportacaoEntity, Integer> {
}

package softwave.backend.backend_mobile.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softwave.backend.backend_mobile.Entity.NotificacaoEntity;

@Repository
public interface NotificacaoRepository extends JpaRepository<NotificacaoEntity, Integer> {

    Page<NotificacaoEntity> findByUsuario_IdOrderByDataCriacaoDesc(Integer usuarioId, Pageable pageable);

    long countByUsuario_IdAndLidaIsFalse(Integer usuarioId);
}

package softwave.backend.backend_mobile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softwave.backend.backend_mobile.Entity.StatusHistoricoEntity;

@Repository
public interface StatusHistoricoRepository extends JpaRepository<StatusHistoricoEntity, Integer> {
    void deleteByTransacao_Id(Integer transacaoId);
}


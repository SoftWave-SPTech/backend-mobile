package softwave.backend.backend_mobile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softwave.backend.backend_mobile.Entity.ComprovanteEntity;

import java.util.List;

@Repository
public interface ComprovanteRepository extends JpaRepository<ComprovanteEntity, Integer> {
    List<ComprovanteEntity> findByTransacaoId(Integer transacaoId);
}

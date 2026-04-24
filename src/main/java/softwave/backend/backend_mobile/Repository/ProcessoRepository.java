package softwave.backend.backend_mobile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softwave.backend.backend_mobile.Entity.ProcessoEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessoRepository extends JpaRepository<ProcessoEntity, Integer> {
    Optional<ProcessoEntity> findByNumeroProcesso(String numeroProcesso);

    List<ProcessoEntity> findByTituloContainingIgnoreCase(String titulo);

    List<ProcessoEntity> findByClienteId(Integer clienteId);
}

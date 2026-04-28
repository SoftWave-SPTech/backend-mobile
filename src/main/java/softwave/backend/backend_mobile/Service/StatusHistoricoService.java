package softwave.backend.backend_mobile.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softwave.backend.backend_mobile.Entity.StatusHistoricoEntity;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Repository.StatusHistoricoRepository;

import java.time.LocalDateTime;

@Service
public class StatusHistoricoService {

    private final StatusHistoricoRepository statusHistoricoRepository;

    public StatusHistoricoService(StatusHistoricoRepository statusHistoricoRepository) {
        this.statusHistoricoRepository = statusHistoricoRepository;
    }

    @Transactional
    public void registrar(TransacaoEntity t, String anterior, String novo, Integer usuarioId, String motivo) {
        if (t == null || novo == null || novo.isBlank()) {
            return;
        }
        StatusHistoricoEntity h = new StatusHistoricoEntity();
        h.setTransacao(t);
        h.setStatusAnterior(anterior);
        h.setStatusNovo(novo);
        h.setUsuarioId(usuarioId);
        h.setMotivo(motivo);
        h.setDataCriacao(LocalDateTime.now());
        statusHistoricoRepository.save(h);
    }
}


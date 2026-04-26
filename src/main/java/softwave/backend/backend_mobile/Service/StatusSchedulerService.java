package softwave.backend.backend_mobile.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class StatusSchedulerService {

    private final TransacaoRepository transacaoRepository;
    private final StatusHistoricoService statusHistoricoService;

    public StatusSchedulerService(
            TransacaoRepository transacaoRepository,
            StatusHistoricoService statusHistoricoService
    ) {
        this.transacaoRepository = transacaoRepository;
        this.statusHistoricoService = statusHistoricoService;
    }

    /** Virada diária: pendente vencida -> atrasado. */
    @Scheduled(cron = "${app.scheduler.atraso-cron:0 0 2 * * *}")
    @Transactional
    public void atualizarPendentesAtrasadas() {
        List<TransacaoEntity> candidatas = transacaoRepository.findByDataVencimentoBeforeAndDataPagamentoIsNull(LocalDate.now());
        for (TransacaoEntity t : candidatas) {
            String atual = t.getStatusFinanceiro() != null ? t.getStatusFinanceiro().toLowerCase(Locale.ROOT) : "pendente";
            if ("cancelado".equals(atual) || "pago".equals(atual) || "atrasado".equals(atual)) {
                continue;
            }
            t.setStatusFinanceiro("atrasado");
            transacaoRepository.save(t);
            statusHistoricoService.registrar(t, atual, "atrasado", null, "Virada automática diária");
        }
    }
}


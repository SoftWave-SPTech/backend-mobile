package softwave.backend.backend_mobile.internal.dto;

import java.time.LocalDate;
import java.util.Map;

public record TransacaoResumoInternoDto(
        long tenantId,
        LocalDate dataInicio,
        LocalDate dataFim,
        double receitaTotal,
        double despesaTotal,
        double ticketMedio,
        long quantidadeTransacoes,
        Map<String, Double> receitaPorCategoria,
        Map<String, Double> despesaPorCategoria
) {}

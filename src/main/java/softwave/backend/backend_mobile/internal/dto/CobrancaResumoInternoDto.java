package softwave.backend.backend_mobile.internal.dto;

import java.time.LocalDate;

public record CobrancaResumoInternoDto(
        long tenantId,
        LocalDate dataInicio,
        LocalDate dataFim,
        double inadimplenciaPercentual,
        double valorRecebido,
        double valorVencido,
        double valorAVencer,
        long quantidadeTitulosAbertos
) {}

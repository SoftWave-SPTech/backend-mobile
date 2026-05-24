package softwave.backend.backend_mobile.internal.dto;

import java.time.LocalDate;
import java.util.List;

public record RankingReceitaResponseDto(
        long tenantId,
        LocalDate dataInicio,
        LocalDate dataFim,
        List<RankingClienteItemDto> itens
) {}

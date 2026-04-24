package softwave.backend.backend_mobile.v1.dto;

import java.time.LocalDate;

public record TransacaoCreateRequest(
        String tipo,
        Long valor,
        String categoria,
        String descricao,
        Integer clienteId,
        Integer processoId,
        Integer honorarioId,
        LocalDate data,
        LocalDate vencimento,
        String status,
        String recorrencia,
        Integer duracaoMeses,
        /** Título exibido (alias do que o app envia como `titulo`). */
        String titulo,
        /** Nome da contraparte quando não há `clienteId`. */
        String contraparte,
        /** Quando true, cria honorário/transação sem processo (somente advogado dono enxerga na listagem). */
        Boolean semProcesso
) {}

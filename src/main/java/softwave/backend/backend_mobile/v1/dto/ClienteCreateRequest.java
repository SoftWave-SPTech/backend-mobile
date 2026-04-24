package softwave.backend.backend_mobile.v1.dto;

import java.util.List;

public record ClienteCreateRequest(
        String nome,
        String email,
        String telefone,
        List<Integer> processoIds
) {}

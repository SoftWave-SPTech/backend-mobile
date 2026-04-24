package softwave.backend.backend_mobile.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorBody(boolean erro, String codigo, String mensagem) {
}

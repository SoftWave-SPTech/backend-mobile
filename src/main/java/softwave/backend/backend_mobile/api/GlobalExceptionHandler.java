package softwave.backend.backend_mobile.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import softwave.backend.backend_mobile.Exception.BadRequestException;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Exception.NotFoundException;
import softwave.backend.backend_mobile.api.dto.ApiErrorBody;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorBody> notFound(NotFoundException e) {
        return err(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiErrorBody> badRequest(RuntimeException e) {
        return err(HttpStatus.UNPROCESSABLE_ENTITY, "CAMPOS_INVALIDOS", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorBody> validation(MethodArgumentNotValidException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = fe != null ? fe.getDefaultMessage() : "Dados inválidos";
        return err(HttpStatus.UNPROCESSABLE_ENTITY, "CAMPOS_INVALIDOS", msg);
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ApiErrorBody> forbidden(Exception e) {
        return err(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", e.getMessage());
    }

    @ExceptionHandler({AuthenticationException.class, JwtException.class, BadCredentialsException.class})
    public ResponseEntity<ApiErrorBody> unauthorized(Exception e) {
        return err(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO", "Token de autenticação inválido ou expirado.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorBody> notFoundRoute(NoResourceFoundException e) {
        return err(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", "Recurso não encontrado");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorBody> generic(Exception e) {
        return err(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", "Erro interno do servidor.");
    }

    private static ResponseEntity<ApiErrorBody> err(HttpStatus status, String codigo, String mensagem) {
        return ResponseEntity.status(status).body(new ApiErrorBody(true, codigo, mensagem));
    }
}

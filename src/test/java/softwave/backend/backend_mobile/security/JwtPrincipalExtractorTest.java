package softwave.backend.backend_mobile.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPrincipalExtractorTest {

    private Jwt buildJwt(String tipoUsuario, String authorities) {
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(120),
                Map.of("alg", "HS512"),
                Map.of(
                        "sub", "u@test.com",
                        "id", 1,
                        "tipoUsuario", tipoUsuario,
                        "authorities", authorities
                )
        );
    }

    @Test
    void advogadoFisicoComRoleAdvogado() {
        Jwt j = buildJwt("advogadoFisico", "ROLE_ADVOGADO");
        assertThat(JwtPrincipalExtractor.isAdvogado(j)).isTrue();
        assertThat(JwtPrincipalExtractor.isCliente(j)).isFalse();
        assertThat(JwtPrincipalExtractor.userId(j)).isEqualTo(1);
        assertThat(JwtPrincipalExtractor.tipoUsuario(j)).isEqualTo("advogadoFisico");
    }

    @Test
    void usuarioFisicoComRoleUsuario() {
        Jwt j = buildJwt("usuario_fisico", "ROLE_USUARIO");
        assertThat(JwtPrincipalExtractor.isCliente(j)).isTrue();
        assertThat(JwtPrincipalExtractor.isAdvogado(j)).isFalse();
    }

    @Test
    void usuarioJuridicoCamelCase() {
        Jwt j = buildJwt("usuarioJuridico", "ROLE_USUARIO");
        assertThat(JwtPrincipalExtractor.isCliente(j)).isTrue();
        assertThat(JwtPrincipalExtractor.isAdvogado(j)).isFalse();
    }
}

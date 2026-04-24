package softwave.backend.backend_mobile.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Locale;

public final class JwtPrincipalExtractor {

    private JwtPrincipalExtractor() {}

    /**
     * Claim "id" emitido pela API-AUTH-MAIL (LoginUseCase / GerenciadorTokenJwt).
     */
    public static Integer userId(Jwt jwt) {
        Object id = jwt.getClaim("id");
        if (id instanceof Number n) {
            return n.intValue();
        }
        if (id instanceof String s) {
            return Integer.parseInt(s);
        }
        throw new IllegalStateException("JWT sem claim id");
    }

    public static String tipoUsuario(Jwt jwt) {
        String t = jwt.getClaimAsString("tipoUsuario");
        return t != null ? t : "";
    }

    /**
     * Normaliza {@code advogado_fisico}, {@code advogadoFisico}, {@code AdvogadoJuridico}, FQCN, etc.
     */
    private static String compactTipoUsuario(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String simple = raw.contains(".") ? raw.substring(raw.lastIndexOf('.') + 1) : raw;
        return simple.replace("_", "").toLowerCase(Locale.ROOT);
    }

    public static boolean isAdvogado(Jwt jwt) {
        String t = tipoUsuario(jwt);
        String c = compactTipoUsuario(t);
        if (c.equals("usuariofisico") || c.equals("usuariojuridico")) {
            return false;
        }
        if (c.equals("advogadofisico") || c.equals("advogadojuridico")) {
            return true;
        }
        if (!t.isBlank() && (t.contains("advogado") || t.contains("Advogado"))) {
            return true;
        }
        String auth = jwt.getClaimAsString("authorities");
        return auth != null && auth.contains("ROLE_ADVOGADO");
    }

    public static boolean isCliente(Jwt jwt) {
        String t = tipoUsuario(jwt);
        String c = compactTipoUsuario(t);
        if (c.equals("advogadofisico") || c.equals("advogadojuridico")) {
            return false;
        }
        if (c.equals("usuariofisico") || c.equals("usuariojuridico")) {
            return true;
        }
        if (!t.isBlank()) {
            String lower = t.toLowerCase(Locale.ROOT);
            if (lower.contains("usuario_fisico") || lower.contains("usuario_juridico")) {
                return true;
            }
            if (t.contains("UsuarioFisico") || t.contains("UsuarioJuridico")) {
                return true;
            }
        }
        String auth = jwt.getClaimAsString("authorities");
        return auth != null && auth.contains("ROLE_USUARIO");
    }
}

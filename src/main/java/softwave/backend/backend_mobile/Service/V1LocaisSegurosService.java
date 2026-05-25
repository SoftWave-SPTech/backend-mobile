package softwave.backend.backend_mobile.Service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softwave.backend.backend_mobile.Entity.LocalSeguroEntity;
import softwave.backend.backend_mobile.Entity.UsuarioEntity;
import softwave.backend.backend_mobile.Exception.BadRequestException;
import softwave.backend.backend_mobile.Exception.ForbiddenException;
import softwave.backend.backend_mobile.Exception.NotFoundException;
import softwave.backend.backend_mobile.Repository.LocalSeguroRepository;
import softwave.backend.backend_mobile.Repository.UsuarioRepository;
import softwave.backend.backend_mobile.security.JwtPrincipalExtractor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class V1LocaisSegurosService {

    private final LocalSeguroRepository localSeguroRepository;
    private final UsuarioRepository usuarioRepository;

    public V1LocaisSegurosService(
            LocalSeguroRepository localSeguroRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.localSeguroRepository = localSeguroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listar(Jwt jwt) {
        int uid = JwtPrincipalExtractor.userId(jwt);
        garantirAdvogado(jwt);
        UsuarioEntity u = usuarioRepository.findById(uid).orElseThrow();
        List<Map<String, Object>> locais = localSeguroRepository.findByUsuario_IdOrderByCreatedAtDesc(uid)
                .stream()
                .map(this::toMap)
                .toList();
        return Map.of(
                "enabled", Boolean.TRUE.equals(u.getLocaisSegurosAtivo()),
                "locais", locais
        );
    }

    @Transactional
    public Map<String, Object> atualizarConfig(Jwt jwt, Map<String, Object> body) {
        int uid = JwtPrincipalExtractor.userId(jwt);
        garantirAdvogado(jwt);
        UsuarioEntity u = usuarioRepository.findById(uid).orElseThrow();
        if (body.containsKey("enabled")) {
            u.setLocaisSegurosAtivo(Boolean.parseBoolean(String.valueOf(body.get("enabled"))));
            usuarioRepository.save(u);
        }
        return Map.of("mensagem", "Configuração atualizada.", "enabled", Boolean.TRUE.equals(u.getLocaisSegurosAtivo()));
    }

    @Transactional
    public Map<String, Object> criar(Jwt jwt, Map<String, Object> body) {
        int uid = JwtPrincipalExtractor.userId(jwt);
        garantirAdvogado(jwt);
        UsuarioEntity u = usuarioRepository.findById(uid).orElseThrow();
        LocalSeguroEntity e = fromBody(body);
        e.setUsuario(u);
        localSeguroRepository.save(e);
        return Map.of("mensagem", "Local seguro cadastrado.", "local", toMap(e));
    }

    @Transactional
    public Map<String, Object> atualizar(Jwt jwt, String idStr, Map<String, Object> body) {
        int uid = JwtPrincipalExtractor.userId(jwt);
        garantirAdvogado(jwt);
        int id = parseId(idStr);
        LocalSeguroEntity e = localSeguroRepository.findByIdAndUsuario_Id(id, uid)
                .orElseThrow(() -> new NotFoundException("Local seguro não encontrado"));
        applyBody(e, body);
        localSeguroRepository.save(e);
        return Map.of("mensagem", "Local seguro atualizado.", "local", toMap(e));
    }

    @Transactional
    public Map<String, Object> excluir(Jwt jwt, String idStr) {
        int uid = JwtPrincipalExtractor.userId(jwt);
        garantirAdvogado(jwt);
        int id = parseId(idStr);
        LocalSeguroEntity e = localSeguroRepository.findByIdAndUsuario_Id(id, uid)
                .orElseThrow(() -> new NotFoundException("Local seguro não encontrado"));
        localSeguroRepository.delete(e);
        return Map.of("mensagem", "Local seguro excluído.");
    }

    private void garantirAdvogado(Jwt jwt) {
        if (!JwtPrincipalExtractor.isAdvogado(jwt)) {
            throw new ForbiddenException("Locais seguros disponíveis apenas para o escritório.");
        }
    }

    private static int parseId(String idStr) {
        if (idStr == null || idStr.isBlank()) {
            throw new BadRequestException("Id inválido");
        }
        String raw = idStr.startsWith("loc_") ? idStr.substring(4) : idStr;
        return Integer.parseInt(raw);
    }

    private LocalSeguroEntity fromBody(Map<String, Object> body) {
        LocalSeguroEntity e = new LocalSeguroEntity();
        applyBody(e, body);
        return e;
    }

    private void applyBody(LocalSeguroEntity e, Map<String, Object> body) {
        String nome = requiredString(body, "nome");
        String cep = normalizeCep(requiredString(body, "cep"));
        String logradouro = requiredString(body, "logradouro");
        String numero = requiredString(body, "numero");
        String cidade = requiredString(body, "cidade");
        String uf = requiredString(body, "uf").toUpperCase();
        double lat = requiredDouble(body, "latitude");
        double lon = requiredDouble(body, "longitude");
        int raio = body.containsKey("raio") ? parseInt(body.get("raio"), 100) : 100;
        if (raio < 50 || raio > 500) {
            throw new BadRequestException("Raio deve estar entre 50 e 500 metros.");
        }

        String complemento = optionalString(body, "complemento");
        String endereco = optionalString(body, "endereco");
        if (endereco == null || endereco.isBlank()) {
            endereco = montarEnderecoExibicao(logradouro, numero, complemento, cidade, uf);
        }

        e.setNome(nome);
        e.setCep(cep);
        e.setLogradouro(logradouro);
        e.setNumero(numero);
        e.setComplemento(complemento);
        e.setCidade(cidade);
        e.setUf(uf);
        e.setEnderecoExibicao(endereco);
        e.setLatitude(lat);
        e.setLongitude(lon);
        e.setRaioMetros(raio);
        e.setAtivo(true);
    }

    private static String montarEnderecoExibicao(
            String logradouro,
            String numero,
            String complemento,
            String cidade,
            String uf
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(logradouro).append(", ").append(numero);
        if (complemento != null && !complemento.isBlank()) {
            sb.append(" - ").append(complemento);
        }
        sb.append(" - ").append(cidade).append("/").append(uf);
        return sb.toString();
    }

    private static String normalizeCep(String cep) {
        String digits = cep.replaceAll("\\D", "");
        if (digits.length() != 8) {
            throw new BadRequestException("CEP inválido.");
        }
        return digits;
    }

    private Map<String, Object> toMap(LocalSeguroEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "loc_" + e.getId());
        m.put("nome", e.getNome());
        m.put("cep", e.getCep());
        m.put("logradouro", e.getLogradouro());
        m.put("numero", e.getNumero());
        m.put("complemento", e.getComplemento() != null ? e.getComplemento() : "");
        m.put("cidade", e.getCidade());
        m.put("uf", e.getUf());
        m.put("endereco", e.getEnderecoExibicao());
        m.put("latitude", e.getLatitude());
        m.put("longitude", e.getLongitude());
        m.put("raio", e.getRaioMetros());
        m.put("ativo", Boolean.TRUE.equals(e.getAtivo()));
        return m;
    }

    private static String requiredString(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null || String.valueOf(v).trim().isEmpty()) {
            throw new BadRequestException("Campo obrigatório: " + key);
        }
        return String.valueOf(v).trim();
    }

    private static String optionalString(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return null;
        return String.valueOf(v).trim();
    }

    private static double requiredDouble(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) {
            throw new BadRequestException("Campo obrigatório: " + key);
        }
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        return Double.parseDouble(String.valueOf(v));
    }

    private static int parseInt(Object v, int defaultVal) {
        if (v == null) return defaultVal;
        if (v instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(v));
    }
}

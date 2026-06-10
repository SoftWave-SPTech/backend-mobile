package softwave.backend.backend_mobile.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class LocalStorageService {

    @Value("${app.storage.upload-dir:./local-storage}")
    private String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostConstruct
    public void prepararEstruturaLocal() throws IOException {

        Path base = Paths.get(uploadDir).toAbsolutePath();

        Files.createDirectories(base);

        for (String pasta : List.of(
                "imagens",
                "documentos",
                "comprovantes",
                "comprovantes-cliente",
                "perfil"
        )) {
            Files.createDirectories(base.resolve(pasta));
        }
    }

    /**
     * Salva o arquivo e retorna o caminho relativo dentro do diretório de upload
     * (ex.: {@code comprovantes-cliente/comprovantes-cliente_uuid.jpg}).
     */
    public String salvar(MultipartFile arquivo, String pasta)
            throws IOException {

        String pastaSeguro =
                (pasta == null || pasta.isBlank())
                        ? "documentos"
                        : pasta.trim();

        String original = arquivo.getOriginalFilename();

        String ext =
                original != null && original.contains(".")
                        ? original.substring(original.lastIndexOf('.'))
                        : ".png";

        String nome =
                pastaSeguro + "_" + UUID.randomUUID() + ext;

        Path dir = Paths.get(uploadDir, pastaSeguro).toAbsolutePath();

        Files.createDirectories(dir);

        Path dest = dir.resolve(nome);

        arquivo.transferTo(dest.toFile());

        return pastaSeguro + "/" + nome;
    }

    /** URL pública para exibição no app (fotos de perfil, etc.). */
    public String urlPublica(String caminhoRelativoOuUrl) {
        if (caminhoRelativoOuUrl == null || caminhoRelativoOuUrl.isBlank()) {
            return caminhoRelativoOuUrl;
        }
        String valor = caminhoRelativoOuUrl.trim().replace('\\', '/');
        if (valor.startsWith("http://") || valor.startsWith("https://")) {
            return valor;
        }
        if (valor.startsWith("/arquivos/")) {
            return baseUrl + valor;
        }
        if (valor.startsWith("arquivos/")) {
            return baseUrl + "/" + valor;
        }
        if (valor.startsWith("/")) {
            valor = valor.substring(1);
        }
        return baseUrl + "/arquivos/" + valor;
    }

    /**
     * Resolve caminho no disco a partir do valor persistido (caminho relativo ou URL legada).
     */
    public Path resolverPath(String caminhoArmazenado) {
        if (caminhoArmazenado == null || caminhoArmazenado.isBlank()) {
            throw new IllegalArgumentException("Caminho do arquivo não informado.");
        }

        String normalizado = caminhoArmazenado.trim().replace('\\', '/');

        if (normalizado.startsWith("file://")) {
            normalizado = normalizado.substring("file://".length());
        }

        int idxArquivos = normalizado.indexOf("/arquivos/");
        if (idxArquivos >= 0) {
            normalizado = normalizado.substring(idxArquivos + "/arquivos/".length());
        } else if (normalizado.startsWith("http://") || normalizado.startsWith("https://")) {
            try {
                String path = URI.create(normalizado).getPath();
                if (path != null && path.startsWith("/arquivos/")) {
                    normalizado = path.substring("/arquivos/".length());
                }
            } catch (IllegalArgumentException ignored) {
                // Mantém valor original para tentativa relativa abaixo.
            }
        }

        while (normalizado.startsWith("/")) {
            normalizado = normalizado.substring(1);
        }

        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path resolved = base.resolve(normalizado).normalize();
        if (!resolved.startsWith(base)) {
            throw new SecurityException("Caminho de arquivo inválido.");
        }
        return resolved;
    }

    public String detectarContentType(Path path, String nomeArquivo) {
        try {
            String probed = Files.probeContentType(path);
            if (probed != null && !probed.isBlank() && !"application/octet-stream".equalsIgnoreCase(probed)) {
                return probed;
            }
        } catch (IOException ignored) {
            // Usa extensão abaixo.
        }

        String nome = (nomeArquivo != null && !nomeArquivo.isBlank())
                ? nomeArquivo
                : path.getFileName().toString();
        String lower = nome.toLowerCase();

        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".heic") || lower.endsWith(".heif")) return "image/jpeg";
        if (lower.endsWith(".pdf")) return "application/pdf";

        return "application/octet-stream";
    }
}

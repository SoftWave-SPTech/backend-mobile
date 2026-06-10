package softwave.backend.backend_mobile.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
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
                "perfil"
        )) {
            Files.createDirectories(base.resolve(pasta));
        }
    }

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

        return baseUrl +
                "/arquivos/" +
                pastaSeguro +
                "/" +
                nome;
    }

    public Path resolverPathArquivo(String caminhoOuUrl) {
        if (caminhoOuUrl == null || caminhoOuUrl.isBlank()) {
            throw new IllegalArgumentException("Caminho do arquivo vazio");
        }

        String relative = caminhoOuUrl.trim();
        int marker = relative.indexOf("/arquivos/");
        if (marker >= 0) {
            relative = relative.substring(marker + "/arquivos/".length());
        } else {
            Path direct = Paths.get(relative);
            if (Files.exists(direct)) {
                return direct.toAbsolutePath().normalize();
            }
        }

        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path resolved = base.resolve(relative).normalize();
        if (!resolved.startsWith(base)) {
            throw new IllegalArgumentException("Caminho do arquivo inválido");
        }
        return resolved;
    }
}
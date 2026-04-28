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

    @Value("${app.storage.upload-dir:./uploads}")
    private String uploadDir;

    @PostConstruct
    public void prepararEstruturaLocal() throws IOException {
        Path base = Paths.get(uploadDir);
        Files.createDirectories(base);
        for (String pasta : List.of("imagens", "documentos", "comprovantes", "perfis")) {
            Files.createDirectories(base.resolve(pasta));
        }
    }

    public String salvar(MultipartFile arquivo, String pasta) throws IOException {
        String pastaSeguro = (pasta == null || pasta.isBlank()) ? "documentos" : pasta.trim();
        String original = arquivo.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf('.'))
                : "";
        String nome = pastaSeguro + "_" + UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir, pastaSeguro);
        Files.createDirectories(dir);
        Path dest = dir.resolve(nome);
        arquivo.transferTo(dest);
        return dest.toAbsolutePath().toString().replace("\\", "/");
    }
}

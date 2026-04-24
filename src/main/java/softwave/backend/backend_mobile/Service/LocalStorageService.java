package softwave.backend.backend_mobile.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService {

    @Value("${app.storage.upload-dir:./uploads}")
    private String uploadDir;

    public String salvar(MultipartFile arquivo, String pasta) throws IOException {
        String original = arquivo.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf('.'))
                : "";
        String nome = pasta + "_" + UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir, pasta);
        Files.createDirectories(dir);
        Path dest = dir.resolve(nome);
        arquivo.transferTo(dest);
        return dest.toAbsolutePath().toString().replace("\\", "/");
    }
}

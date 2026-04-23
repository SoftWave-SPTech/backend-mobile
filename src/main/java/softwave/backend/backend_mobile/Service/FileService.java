package softwave.backend.backend_mobile.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {

    @Value("${upload.dir}")
    private String uploadDir;

    public String salvarImagem(MultipartFile file) throws IOException {

        String basePath = System.getProperty("user.dir");

        File pasta = new File(basePath + "/" + uploadDir);

        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        String nomeArquivo = UUID.randomUUID() + "_" + file.getOriginalFilename();

        File destino = new File(pasta, nomeArquivo);

        file.transferTo(destino);

        return nomeArquivo;
    }
}
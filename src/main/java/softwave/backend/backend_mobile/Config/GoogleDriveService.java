package softwave.backend.backend_mobile.Config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

import java.io.FileInputStream;
import java.util.Collections;

@Service
public class GoogleDriveService {

    private static final String APPLICATION_NAME = "Backend Comprovantes";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String FOLDER_ID = "1AbCxyz123456";

    private Drive getDriveService() throws Exception {

        GoogleCredential credential = GoogleCredential
                .fromStream(new FileInputStream("credenciais.json"))
                .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential
        ).setApplicationName(APPLICATION_NAME)
                .build();
    }

    public String uploadArquivo(MultipartFile file, Integer transacaoId) throws Exception {

        Drive service = getDriveService();

        String nomeOriginal = file.getOriginalFilename();
        String extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));

        // 🔥 nome inteligente
        String nomeUnico = "transacao_" + transacaoId + "_" + UUID.randomUUID() + extensao;

        File fileMetadata = new File();
        fileMetadata.setName(nomeUnico);

        // 🔥 salvar na pasta específica
        fileMetadata.setParents(Collections.singletonList(FOLDER_ID));

        java.io.File tempFile = java.io.File.createTempFile("upload", nomeUnico);
        file.transferTo(tempFile);

        FileContent mediaContent = new FileContent(
                file.getContentType(),
                tempFile
        );

        File uploadedFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        return uploadedFile.getId();
    }

    public void deletarArquivo(String fileId) throws Exception {

        Drive service = getDriveService();

        service.files().delete(fileId).execute();
    }
}

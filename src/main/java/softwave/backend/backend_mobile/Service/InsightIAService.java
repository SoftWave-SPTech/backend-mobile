package softwave.backend.backend_mobile.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import softwave.backend.backend_mobile.DTO.InsightExternalRequestDTO;
import softwave.backend.backend_mobile.DTO.InsightExternalResponseDTO;
import softwave.backend.backend_mobile.DTO.InsightIARequestDTO;
import softwave.backend.backend_mobile.DTO.InsightIAResponseDTO;
import softwave.backend.backend_mobile.Entity.InsightIAEntity;
import softwave.backend.backend_mobile.Repository.InsightIARepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class InsightIAService {

    private final InsightIARepository repository;
    private final RestTemplate restTemplate;

    private final String URL_MICROSERVICO = "http://localhost:5000/insight";

    public InsightIAService(InsightIARepository repository,
                            RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    // 🔹 CRUD

    public List<InsightIAResponseDTO> listar() {
        return repository.findAll()
                .stream()
                .map(InsightIAResponseDTO::new)
                .toList();
    }

    public InsightIAResponseDTO buscarPorId(Integer id) {
        return new InsightIAResponseDTO(buscarEntity(id));
    }

    public InsightIAResponseDTO criar(InsightIARequestDTO dto) {

        InsightIAEntity entity = new InsightIAEntity();

        entity.setTipoAnalise(dto.getTipoAnalise());
        entity.setDataInicio(dto.getDataInicio());
        entity.setDataFim(dto.getDataFim());
        entity.setDataGeracao(LocalDate.now());

        return new InsightIAResponseDTO(repository.save(entity));
    }

    public InsightIAResponseDTO atualizar(Integer id, InsightIARequestDTO dto) {

        InsightIAEntity entity = buscarEntity(id);

        entity.setTipoAnalise(dto.getTipoAnalise());
        entity.setDataInicio(dto.getDataInicio());
        entity.setDataFim(dto.getDataFim());

        return new InsightIAResponseDTO(repository.save(entity));
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    // 🔥 GERAR INSIGHT (integração com microserviço)

    public InsightIAResponseDTO gerarInsight(InsightIARequestDTO dto) {

        // 🔹 montar payload
        InsightExternalRequestDTO request = new InsightExternalRequestDTO();
        request.setTipoAnalise(dto.getTipoAnalise());
        request.setDataInicio(dto.getDataInicio());
        request.setDataFim(dto.getDataFim());

        // 🔹 chamar microserviço
        InsightExternalResponseDTO response = restTemplate.postForObject(
                URL_MICROSERVICO,
                request,
                InsightExternalResponseDTO.class
        );

        // 🔹 salvar resultado
        InsightIAEntity entity = new InsightIAEntity();

        entity.setTipoAnalise(dto.getTipoAnalise());
        entity.setDataInicio(dto.getDataInicio());
        entity.setDataFim(dto.getDataFim());
        entity.setResultadoTexto(response != null ? response.getResultado() : "Sem resposta");
        entity.setDataGeracao(LocalDate.now());

        return new InsightIAResponseDTO(repository.save(entity));
    }

    // 🔹 helper
    private InsightIAEntity buscarEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insight não encontrado"));
    }
}

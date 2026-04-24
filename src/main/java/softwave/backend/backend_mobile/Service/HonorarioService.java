package softwave.backend.backend_mobile.Service;

import org.springframework.stereotype.Service;
import softwave.backend.backend_mobile.DTO.HonorarioRequestDTO;
import softwave.backend.backend_mobile.DTO.HonorarioResponseDTO;
import softwave.backend.backend_mobile.DTO.TransacaoResponseDTO;
import softwave.backend.backend_mobile.Entity.*;
import softwave.backend.backend_mobile.Repository.HonorarioRepository;
import softwave.backend.backend_mobile.Repository.ProcessoRepository;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class HonorarioService {

    private final HonorarioRepository repository;
    private final TransacaoRepository transacaoRepository;
    private final ProcessoRepository processoRepository;

    public HonorarioService(HonorarioRepository repository,
                            TransacaoRepository transacaoRepository, ProcessoRepository processoRepository) {
        this.repository = repository;
        this.transacaoRepository = transacaoRepository;
        this.processoRepository = processoRepository;
    }

    // 🔹 CRUD

    public List<HonorarioResponseDTO> listar() {
        return repository.findAll()
                .stream()
                .map(HonorarioResponseDTO::new)
                .toList();
    }

    public HonorarioResponseDTO buscarPorId(Integer id) {
        return new HonorarioResponseDTO(buscarEntity(id));
    }

    public HonorarioResponseDTO criar(HonorarioRequestDTO dto) {

        HonorarioEntity h = new HonorarioEntity();

        h.setTitulo(dto.getTitulo());
        h.setValorTotal(dto.getValorTotal());
        h.setDataInicio(dto.getDataInicio());
        h.setDataFim(dto.getDataFim());
        h.setParcelas(dto.getParcelas());
        h.setStatus("PENDENTE");
        ProcessoEntity processo = processoRepository.findById(dto.getProcessoId())
                .orElseThrow(() -> new RuntimeException("Processo não encontrado"));

        h.setProcesso(processo);

        return new HonorarioResponseDTO(repository.save(h));
    }

    public HonorarioResponseDTO atualizar(Integer id, HonorarioRequestDTO dto) {

        HonorarioEntity h = buscarEntity(id);

        h.setTitulo(dto.getTitulo());
        h.setValorTotal(dto.getValorTotal());
        h.setDataInicio(dto.getDataInicio());
        h.setDataFim(dto.getDataFim());
        h.setParcelas(dto.getParcelas());

        return new HonorarioResponseDTO(repository.save(h));
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    // 🔹 REGRAS DE NEGÓCIO

    // ✅ valor pago
    public Double calcularValorPago(Integer honorarioId) {

        return transacaoRepository.findAll().stream()
                .filter(t -> t.getHonorario() != null &&
                        t.getHonorario().getId().equals(honorarioId))
                .filter(t -> "PAGO".equals(t.getStatusFinanceiro()))
                .mapToDouble(TransacaoEntity::getValor)
                .sum();
    }

    // ✅ valor pendente
    public Double calcularValorPendente(Integer honorarioId) {

        HonorarioEntity h = buscarEntity(honorarioId);

        Double pago = calcularValorPago(honorarioId);

        return h.getValorTotal() - pago;
    }

    // ✅ gerar parcelas
    public List<TransacaoResponseDTO> gerarParcelas(Integer honorarioId, Integer quantidadeParcelas) {

        HonorarioEntity h = buscarEntity(honorarioId);

        Double valorParcela = h.getValorTotal() / quantidadeParcelas;

        List<TransacaoResponseDTO> lista = new ArrayList<>();

        for (int i = 1; i <= quantidadeParcelas; i++) {

            TransacaoEntity t = new TransacaoEntity();

            t.setTitulo("Parcela " + i + " - " + h.getTitulo());
            t.setValor(valorParcela);
            t.setTipo("RECEITA");
            t.setStatusFinanceiro(StatusFinanceiro.valueOf("PENDENTE"));
            t.setStatusAprovacao(StatusAprovacao.valueOf("PENDENTE"));
            t.setDataEmissao(LocalDate.now());
            t.setDataVencimento(LocalDate.now().plusMonths(i));
            t.setHonorario(h);

            lista.add(new TransacaoResponseDTO(transacaoRepository.save(t)));
        }

        return lista;
    }

    // 🔹 helper
    private HonorarioEntity buscarEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Honorário não encontrado"));
    }

    public List<HonorarioResponseDTO> buscarPorProcesso(Integer processoId) {

        List<HonorarioEntity> lista = repository.findByProcessoId(processoId);

        if (lista.isEmpty()) {
            throw new RuntimeException("Nenhum honorário encontrado para esse processo");
        }

        return lista.stream()
                .map(HonorarioResponseDTO::new)
                .toList();
    }
}
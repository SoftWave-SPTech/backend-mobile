package softwave.backend.backend_mobile.Service;

import org.springframework.stereotype.Service;
import softwave.backend.backend_mobile.DTO.TransacaoRequestDTO;
import softwave.backend.backend_mobile.DTO.TransacaoResponseDTO;
import softwave.backend.backend_mobile.Entity.StatusAprovacao;
import softwave.backend.backend_mobile.Entity.StatusFinanceiro;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransacaoService {

    private final TransacaoRepository repository;

    public TransacaoService(TransacaoRepository repository) {
        this.repository = repository;
    }

    // 🔹 CRUD

    public List<TransacaoResponseDTO> listar() {
        return repository.findAll()
                .stream()
                .map(TransacaoResponseDTO::new)
                .toList();
    }

    public TransacaoResponseDTO buscarPorId(Integer id) {
        return new TransacaoResponseDTO(buscarEntity(id));
    }

    public TransacaoResponseDTO criar(TransacaoRequestDTO dto) {

        TransacaoEntity t = new TransacaoEntity();

        t.setTitulo(dto.getTitulo());
        t.setValor(dto.getValor());
        t.setTipo(dto.getTipo());
        t.setDescricao(dto.getDescricao());
        t.setObservacoes(dto.getObservacoes());
        t.setContraparte(dto.getContraparte());

        // regras iniciais
        t.setStatusAprovacao(StatusAprovacao.valueOf("PENDENTE"));
        t.setStatusFinanceiro(StatusFinanceiro.valueOf("PENDENTE"));
        t.setDataEmissao(LocalDate.now());
        t.setDataVencimento(dto.getDataVencimento());

        return new TransacaoResponseDTO(repository.save(t));
    }

    public TransacaoResponseDTO atualizar(Integer id, TransacaoRequestDTO dto) {

        TransacaoEntity t = buscarEntity(id);

        t.setTitulo(dto.getTitulo());
        t.setValor(dto.getValor());
        t.setTipo(dto.getTipo());
        t.setDescricao(dto.getDescricao());
        t.setObservacoes(dto.getObservacoes());
        t.setContraparte(dto.getContraparte());
        t.setDataVencimento(dto.getDataVencimento());

        return new TransacaoResponseDTO(repository.save(t));
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    // 🔹 REGRAS DE NEGÓCIO

    // ✅ gerar cobrança
    public TransacaoResponseDTO gerarCobranca(Integer id) {
        TransacaoEntity t = buscarEntity(id);

        t.setStatusAprovacao(StatusAprovacao.valueOf("PENDENTE"));
        t.setStatusFinanceiro(StatusFinanceiro.valueOf("PENDENTE"));
        t.setDataEmissao(LocalDate.now());

        return new TransacaoResponseDTO(repository.save(t));
    }

    // ✅ aprovar
    public TransacaoResponseDTO aprovar(Integer id) {
        TransacaoEntity t = buscarEntity(id);

        if ("REPROVADO".equals(t.getStatusAprovacao())) {
            throw new RuntimeException("Transação já foi reprovada");
        }

        t.setStatusAprovacao(StatusAprovacao.valueOf("APROVADO"));

        return new TransacaoResponseDTO(repository.save(t));
    }

    // ✅ reprovar
    public TransacaoResponseDTO reprovar(Integer id) {
        TransacaoEntity t = buscarEntity(id);

        if ("APROVADO".equals(t.getStatusAprovacao())) {
            throw new RuntimeException("Transação já foi aprovada");
        }

        t.setStatusAprovacao(StatusAprovacao.valueOf("REPROVADO"));

        return new TransacaoResponseDTO(repository.save(t));
    }

    // ✅ marcar como pago
    public TransacaoResponseDTO marcarComoPago(Integer id) {
        TransacaoEntity t = buscarEntity(id);

        if (!"APROVADO".equals(t.getStatusAprovacao())) {
            throw new RuntimeException("Só pode pagar transações aprovadas");
        }

        t.setStatusFinanceiro(StatusFinanceiro.valueOf("PAGO"));
        t.setDataPagamento(LocalDate.now());

        return new TransacaoResponseDTO(repository.save(t));
    }

    // 🔹 helper
    private TransacaoEntity buscarEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));
    }
}
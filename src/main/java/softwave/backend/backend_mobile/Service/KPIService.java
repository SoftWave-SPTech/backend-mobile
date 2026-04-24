package softwave.backend.backend_mobile.Service;

import org.springframework.stereotype.Service;
import softwave.backend.backend_mobile.DTO.KPIResponseDTO;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;
import softwave.backend.backend_mobile.Repository.TransacaoRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class KPIService {

    private final TransacaoRepository repository;

    public KPIService(TransacaoRepository repository) {
        this.repository = repository;
    }

    private List<TransacaoEntity> getTransacoes() {
        return repository.findAll();
    }

    // 🔹 Receita mensal
    public KPIResponseDTO calcularReceitaMensal() {
        BigDecimal total = getTransacoes().stream()
                .filter(t -> "RECEITA".equals(t.getTipo()))
                .filter(t -> "PAGO".equals(t.getStatusFinanceiro()))
                .map(t -> BigDecimal.valueOf(t.getValor()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new KPIResponseDTO("Receita Mensal", total);
    }

    // 🔹 Despesa mensal
    public KPIResponseDTO calcularDespesaMensal() {
        BigDecimal total = getTransacoes().stream()
                .filter(t -> "DESPESA".equals(t.getTipo()))
                .map(t -> BigDecimal.valueOf(t.getValor()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new KPIResponseDTO("Despesa Mensal", total);
    }

    // 🔹 Total pago cliente
    public KPIResponseDTO calcularTotalPagoCliente() {
        BigDecimal total = getTransacoes().stream()
                .filter(t -> "PAGO".equals(t.getStatusFinanceiro()))
                .map(t -> BigDecimal.valueOf(t.getValor()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new KPIResponseDTO("Total Pago Cliente", total);
    }

    // 🔹 Inadimplência
    public KPIResponseDTO calcularInadimplencia() {
        BigDecimal total = getTransacoes().stream()
                .filter(t -> "PENDENTE".equals(t.getStatusFinanceiro()))
                .map(t -> BigDecimal.valueOf(t.getValor()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new KPIResponseDTO("Inadimplência", total);
    }

    // 🔹 Total honorário receber
    public KPIResponseDTO calcularTotalHonorarioReceber() {
        BigDecimal total = getTransacoes().stream()
                .filter(t -> "RECEITA".equals(t.getTipo()))
                .filter(t -> !"PAGO".equals(t.getStatusFinanceiro()))
                .map(t -> BigDecimal.valueOf(t.getValor()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new KPIResponseDTO("Honorário a Receber", total);
    }

    // 🔹 Total honorário pago
    public KPIResponseDTO calcularTotalHonorarioPago() {
        BigDecimal total = getTransacoes().stream()
                .filter(t -> "RECEITA".equals(t.getTipo()))
                .filter(t -> "PAGO".equals(t.getStatusFinanceiro()))
                .map(t -> BigDecimal.valueOf(t.getValor()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new KPIResponseDTO("Honorário Pago", total);
    }

    // 🔹 Pendente cliente
    public KPIResponseDTO calcularTotalPendenteCliente() {
        BigDecimal total = getTransacoes().stream()
                .filter(t -> "PENDENTE".equals(t.getStatusFinanceiro()))
                .map(t -> BigDecimal.valueOf(t.getValor()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new KPIResponseDTO("Pendente Cliente", total);
    }

    // 🔹 Lucro líquido
    public KPIResponseDTO calcularLucroLiquido() {
        BigDecimal receita = calcularReceitaMensal().getValor();
        BigDecimal despesa = calcularDespesaMensal().getValor();

        return new KPIResponseDTO("Lucro Líquido", receita.subtract(despesa));
    }

    // 🔹 Pendentes
    public KPIResponseDTO calcularPendentes() {
        BigDecimal total = getTransacoes().stream()
                .filter(t -> "PENDENTE".equals(t.getStatusFinanceiro()))
                .map(t -> BigDecimal.valueOf(t.getValor()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new KPIResponseDTO("Pendentes", total);
    }

    // 🔹 Crescimento
    public KPIResponseDTO calcularCrescimento() {
        // versão simples (pode evoluir depois)
        return new KPIResponseDTO("Crescimento", BigDecimal.ZERO);
    }

    // 🔹 Valor disponível
    public KPIResponseDTO calcularValorDisponivel() {
        BigDecimal total = getTransacoes().stream()
                .filter(t -> "PAGO".equals(t.getStatusFinanceiro()))
                .map(t -> BigDecimal.valueOf(t.getValor()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new KPIResponseDTO("Valor Disponível", total);
    }

    // 🔹 Ticket médio
    public KPIResponseDTO calcularTicketMedio() {
        List<TransacaoEntity> pagos = getTransacoes().stream()
                .filter(t -> "PAGO".equals(t.getStatusFinanceiro()))
                .toList();

        if (pagos.isEmpty()) {
            return new KPIResponseDTO("Ticket Médio", BigDecimal.ZERO);
        }

        BigDecimal total = pagos.stream()
                .map(t -> BigDecimal.valueOf(t.getValor()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal media = total.divide(BigDecimal.valueOf(pagos.size()), 2, RoundingMode.HALF_UP);

        return new KPIResponseDTO("Ticket Médio", media);
    }

    // 🔹 Margem lucro
    public KPIResponseDTO calcularMargemLucro() {
        BigDecimal receita = calcularReceitaMensal().getValor();
        BigDecimal lucro = calcularLucroLiquido().getValor();

        if (receita.compareTo(BigDecimal.ZERO) == 0) {
            return new KPIResponseDTO("Margem de Lucro", BigDecimal.ZERO);
        }

        BigDecimal margem = lucro.divide(receita, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return new KPIResponseDTO("Margem de Lucro (%)", margem);
    }
}

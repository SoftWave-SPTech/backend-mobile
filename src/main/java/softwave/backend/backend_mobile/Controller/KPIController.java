package softwave.backend.backend_mobile.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import softwave.backend.backend_mobile.DTO.KPIResponseDTO;
import softwave.backend.backend_mobile.Service.KPIService;

@RestController
@RequestMapping("/kpi")
public class KPIController {

    private final KPIService service;

    public KPIController(KPIService service) {
        this.service = service;
    }

    @GetMapping("/receita")
    public KPIResponseDTO receita() {
        return service.calcularReceitaMensal();
    }

    @GetMapping("/despesa")
    public KPIResponseDTO despesa() {
        return service.calcularDespesaMensal();
    }

    @GetMapping("/pago-cliente")
    public KPIResponseDTO pagoCliente() {
        return service.calcularTotalPagoCliente();
    }

    @GetMapping("/inadimplencia")
    public KPIResponseDTO inadimplencia() {
        return service.calcularInadimplencia();
    }

    @GetMapping("/honorario-receber")
    public KPIResponseDTO honorarioReceber() {
        return service.calcularTotalHonorarioReceber();
    }

    @GetMapping("/honorario-pago")
    public KPIResponseDTO honorarioPago() {
        return service.calcularTotalHonorarioPago();
    }

    @GetMapping("/pendente-cliente")
    public KPIResponseDTO pendenteCliente() {
        return service.calcularTotalPendenteCliente();
    }

    @GetMapping("/lucro")
    public KPIResponseDTO lucro() {
        return service.calcularLucroLiquido();
    }

    @GetMapping("/pendentes")
    public KPIResponseDTO pendentes() {
        return service.calcularPendentes();
    }

    @GetMapping("/disponivel")
    public KPIResponseDTO disponivel() {
        return service.calcularValorDisponivel();
    }

    @GetMapping("/ticket-medio")
    public KPIResponseDTO ticketMedio() {
        return service.calcularTicketMedio();
    }

    @GetMapping("/margem")
    public KPIResponseDTO margem() {
        return service.calcularMargemLucro();
    }
}

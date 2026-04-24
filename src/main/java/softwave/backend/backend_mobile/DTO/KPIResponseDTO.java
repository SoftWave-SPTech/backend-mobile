package softwave.backend.backend_mobile.DTO;

import java.math.BigDecimal;

public class KPIResponseDTO {

    private String nome;
    private BigDecimal valor;

    public KPIResponseDTO(String nome, BigDecimal valor) {
        this.nome = nome;
        this.valor = valor;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getValor() {
        return valor;
    }
}

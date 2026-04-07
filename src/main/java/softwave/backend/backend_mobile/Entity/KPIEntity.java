package softwave.backend.backend_mobile.Entity;



import java.time.LocalDate;

public class KPIEntity {
    private String nome;
    private Double valor;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    public KPIEntity() {
    }

    public KPIEntity(
            String nome,
            LocalDate dataInicio,
            Double valor,
            LocalDate dataFim
    ) {
        this.nome = nome;
        this.dataInicio = dataInicio;
        this.valor = valor;
        this.dataFim = dataFim;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }
}

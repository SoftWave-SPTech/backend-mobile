package softwave.backend.backend_mobile.Repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import softwave.backend.backend_mobile.Entity.HonorarioEntity;
import softwave.backend.backend_mobile.Entity.ProcessoEntity;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Locale;

public final class TransacaoSpecifications {

    private TransacaoSpecifications() {}

    public static Specification<TransacaoEntity> processoEm(Collection<Integer> processoIds) {
        return (root, q, cb) -> root.join("honorario").join("processo").get("id").in(processoIds);
    }

    /** Honorários sem processo criados pelo advogado informado. */
    public static Specification<TransacaoEntity> avulsoDoAdvogado(Integer advogadoUsuarioId) {
        return (root, q, cb) -> {
            Join<TransacaoEntity, HonorarioEntity> h = root.join("honorario");
            return cb.and(
                    h.get("processo").isNull(),
                    cb.equal(h.get("advogadoUsuarioId"), advogadoUsuarioId)
            );
        };
    }

    /** Transações em processos do usuário OU avulsas do advogado. */
    public static Specification<TransacaoEntity> processoEmOuAvulso(Collection<Integer> processoIds, Integer advogadoUsuarioId) {
        return (root, query, cb) -> {
            Join<TransacaoEntity, HonorarioEntity> h = root.join("honorario", JoinType.INNER);
            Join<HonorarioEntity, ProcessoEntity> p = h.join("processo", JoinType.LEFT);
            Predicate emProcesso = cb.and(p.isNotNull(), p.get("id").in(processoIds));
            Predicate avulso = cb.and(p.isNull(), cb.equal(h.get("advogadoUsuarioId"), advogadoUsuarioId));
            return cb.or(emProcesso, avulso);
        };
    }

    public static Specification<TransacaoEntity> tipoReceitaOuDespesa(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return (root, q, cb) -> cb.conjunction();
        }
        String t = tipo.trim().toLowerCase();
        return (root, q, cb) -> cb.like(cb.lower(root.get("tipo")), "%" + t + "%");
    }

    public static Specification<TransacaoEntity> statusFinanceiroOuAprovacao(String status) {
        if (status == null || status.isBlank()) {
            return (root, q, cb) -> cb.conjunction();
        }
        String s = status.trim().toLowerCase(Locale.ROOT).replace("-", "");
        if ("atrasado".equals(s)) {
            return atrasadoOuVencida();
        }
        if ("pendente".equals(s) || "emdia".equals(s)) {
            return pendenteNaoVencida();
        }
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(cb.coalesce(root.get("statusFinanceiro"), "")), "%" + s + "%"),
                cb.like(cb.lower(cb.coalesce(root.get("statusAprovacao"), "")), "%" + s + "%")
        );
    }

    private static Specification<TransacaoEntity> atrasadoOuVencida() {
        LocalDate hoje = LocalDate.now();
        return (root, q, cb) -> {
            Predicate naoPago = cb.isNull(root.get("dataPagamento"));
            Predicate naoCancel = cb.not(cb.like(cb.lower(cb.coalesce(root.get("statusFinanceiro"), "")), "%cancel%"));
            Predicate naoPagoStatus = cb.not(cb.like(cb.lower(cb.coalesce(root.get("statusFinanceiro"), "")), "%pago%"));
            Predicate vencida = cb.and(
                    root.get("dataVencimento").isNotNull(),
                    cb.lessThan(root.get("dataVencimento"), hoje)
            );
            Predicate statusAtrasado = cb.like(cb.lower(cb.coalesce(root.get("statusFinanceiro"), "")), "%atras%");
            return cb.and(naoPago, naoCancel, naoPagoStatus, cb.or(statusAtrasado, vencida));
        };
    }

    private static Specification<TransacaoEntity> pendenteNaoVencida() {
        LocalDate hoje = LocalDate.now();
        return (root, q, cb) -> {
            Predicate naoPago = cb.isNull(root.get("dataPagamento"));
            Predicate naoCancel = cb.not(cb.like(cb.lower(cb.coalesce(root.get("statusFinanceiro"), "")), "%cancel%"));
            Predicate naoPagoStatus = cb.not(cb.like(cb.lower(cb.coalesce(root.get("statusFinanceiro"), "")), "%pago%"));
            Predicate naoAtrasado = cb.not(cb.like(cb.lower(cb.coalesce(root.get("statusFinanceiro"), "")), "%atras%"));
            Predicate naoVencida = cb.or(
                    root.get("dataVencimento").isNull(),
                    cb.greaterThanOrEqualTo(root.get("dataVencimento"), hoje)
            );
            return cb.and(naoPago, naoCancel, naoPagoStatus, naoAtrasado, naoVencida);
        };
    }

    public static Specification<TransacaoEntity> buscaTituloOuCliente(String busca) {
        if (busca == null || busca.isBlank()) {
            return (root, q, cb) -> cb.conjunction();
        }
        String b = "%" + busca.trim().toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(cb.coalesce(root.get("titulo"), "")), b),
                cb.like(cb.lower(cb.coalesce(root.get("contraparte"), "")), b)
        );
    }

    public static Specification<TransacaoEntity> dataEmissaoEntre(LocalDate ini, LocalDate fim) {
        if (ini == null || fim == null) {
            return (root, q, cb) -> cb.conjunction();
        }
        return (root, q, cb) -> cb.between(root.get("dataEmissao"), ini, fim);
    }
}

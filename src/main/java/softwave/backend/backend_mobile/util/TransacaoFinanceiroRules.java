package softwave.backend.backend_mobile.util;

import softwave.backend.backend_mobile.Entity.TransacaoEntity;

import java.util.Locale;

public final class TransacaoFinanceiroRules {

    private TransacaoFinanceiroRules() {}

    public static boolean isReceita(TransacaoEntity t) {
        String tipo = t.getTipo();
        return tipo != null && tipo.toLowerCase(Locale.ROOT).contains("receita");
    }

    public static boolean isDespesa(TransacaoEntity t) {
        String tipo = t.getTipo();
        return tipo != null && tipo.toLowerCase(Locale.ROOT).contains("despesa");
    }

    public static boolean isCancelada(TransacaoEntity t) {
        String status = t.getStatusFinanceiro();
        return status != null && status.toLowerCase(Locale.ROOT).contains("cancel");
    }

    public static boolean estaPago(TransacaoEntity t) {
        if (isCancelada(t)) {
            return false;
        }
        if (t.getDataPagamento() != null) {
            return true;
        }
        String sf = t.getStatusFinanceiro();
        return sf != null && sf.toLowerCase(Locale.ROOT).contains("pago");
    }

    public static boolean contaComoPendente(TransacaoEntity t) {
        if (isCancelada(t)) {
            return false;
        }
        if (!isReceita(t)) {
            return false;
        }
        return !estaPago(t);
    }

    public static String categoriaOrDefault(TransacaoEntity t) {
        String categoria = t.getCategoria();
        if (categoria != null && !categoria.isBlank()) {
            return categoria.trim();
        }
        return "outros";
    }
}

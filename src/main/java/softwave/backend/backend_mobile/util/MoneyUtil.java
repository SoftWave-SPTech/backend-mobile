package softwave.backend.backend_mobile.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtil {

    private MoneyUtil() {}

    public static long toCentavos(BigDecimal brl) {
        if (brl == null) {
            return 0;
        }
        return brl.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    public static BigDecimal fromCentavos(long centavos) {
        return BigDecimal.valueOf(centavos).movePointLeft(2);
    }

    public static BigDecimal toBigDecimalOrZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}

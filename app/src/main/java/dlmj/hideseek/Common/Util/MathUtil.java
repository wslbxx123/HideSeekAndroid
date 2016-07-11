package dlmj.hideseek.Common.Util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by Two on 5/19/16.
 */
public class MathUtil {
    public static String round(float value) {
        DecimalFormat decimalFormat = new DecimalFormat("##0.00");
        return decimalFormat.format(value);
    }
}

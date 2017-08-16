package reporter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class Reporter {
    public abstract void report();

    protected String getPercentage(double numerator, int denominator) {
        double perc = (numerator/(double) denominator)*100;
        Double rounded = BigDecimal.valueOf(perc).setScale(1, RoundingMode.HALF_UP).doubleValue();
        return rounded + "%";
    }

    protected double getAverage(int numerator, int denominator) {
        double avg = numerator / (double) denominator;
        Double rounded = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue();
        return rounded;
    }
}

package com.crafthinge.payment;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Fake processor for a demo project (no real money moves).
 * 4242 4242 4242 4242 -> approved, 4000 0000 0000 0002 -> declined,
 * any number failing the Luhn check -> declined as invalid.
 */
@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public String name() {
        return "mock";
    }

    @Override
    public Result charge(BigDecimal amount, String currency, String cardNumber) {
        String digits = cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");
        if (digits.length() < 12 || !luhnValid(digits)) {
            return new Result(false, null, "Card number is not valid");
        }
        if (digits.endsWith("0002")) {
            return new Result(false, null, "Card was declined");
        }
        return new Result(true, "mock_ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20), null);
    }

    static boolean luhnValid(String digits) {
        int sum = 0;
        boolean dbl = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return sum % 10 == 0;
    }
}

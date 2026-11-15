package dev.refundworkflow.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void normalizesAmountsToCurrencyFractionDigitsWithoutRounding() {
        assertEquals(new BigDecimal("12.30"), Money.of("12.3", "CAD").amount());
        assertEquals(new BigDecimal("1200"), Money.of("1200", "JPY").amount());
    }

    @Test
    void rejectsImplicitRoundingAndNegativeAmounts() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("1.001", "CAD"));
        assertThrows(IllegalArgumentException.class, () -> new Money(new BigDecimal("-0.01"), java.util.Currency.getInstance("CAD")));
    }

    @Test
    void arithmeticRequiresSameCurrencyAndNeverProducesNegativeMoney() {
        Money tenCad = Money.of("10.00", "CAD");
        assertEquals(Money.of("12.50", "CAD"), tenCad.plus(Money.of("2.50", "CAD")));
        assertThrows(IllegalArgumentException.class, () -> tenCad.plus(Money.of("1.00", "USD")));
        assertThrows(IllegalArgumentException.class, () -> tenCad.minus(Money.of("11.00", "CAD")));
    }
}

package org.tenpo.challenge.domain.vo;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class BigDecimalValueObject {
    private final BigDecimal value;

    public BigDecimalValueObject(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BigDecimalValueObject that = (BigDecimalValueObject) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

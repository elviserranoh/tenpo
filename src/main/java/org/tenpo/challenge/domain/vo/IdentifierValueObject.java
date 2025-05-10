package org.tenpo.challenge.domain.vo;

import java.util.Objects;

public class IdentifierValueObject<T> {
    private final T identifier;

    public IdentifierValueObject(T identifier) {
        this.identifier = identifier;
    }

    public T value() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IdentifierValueObject<?> that = (IdentifierValueObject<?>) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}

package com.primalimited.gis;

import java.util.Objects;

class Pair<A, B> {
    private final A aValue;
    private final B bValue;

    public static <A, B> Pair<A, B> with(final A aValue, final B bValue) {
        Objects.requireNonNull(aValue);
        Objects.requireNonNull(bValue);
        return new Pair<>(aValue, bValue);
    }

    public static <A, B> Pair<A, B> empty() {
        return new Pair<>(null, null);
    }

    protected Pair(final A aValue, final B bValue) {
        this.aValue = aValue;
        this.bValue = bValue;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + aValue.toString() + ", " + bValue.toString() + ">";
    }

    @Override
    public int hashCode() {
        return Objects.hash(aValue, bValue);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<?, ?> other = (Pair<?, ?>) obj;
        return this.aValue.equals(other.aValue) && this.bValue.equals(other.bValue);
    }

    public boolean isEmpty() {
        return aValue == null && bValue == null;
    }

    public A getAValue() {
        return aValue;
    }

    public B getBValue() {
        return bValue;
    }
}

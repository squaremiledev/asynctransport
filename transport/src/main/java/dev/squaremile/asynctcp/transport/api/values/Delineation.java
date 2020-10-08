package dev.squaremile.asynctcp.transport.api.values;

import java.util.Objects;

public class Delineation
{
    private final Type type;
    private final int knownLength;
    public Delineation(final Type type, final int knownLength)
    {
        this.type = type;
        this.knownLength = knownLength;
    }

    public int knownLength()
    {
        return knownLength;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, knownLength);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final Delineation that = (Delineation)o;
        return knownLength == that.knownLength &&
               Objects.equals(type, that.type);
    }

    @Override
    public String toString()
    {
        return "Delineation{" +
               "type='" + type + '\'' +
               ", knownLength=" + knownLength +
               '}';
    }

    public enum Type
    {
        FIXED_LENGTH
    }
}

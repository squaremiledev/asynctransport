package dev.squaremile.asynctcp.serialization.api.delineation;

import java.util.Objects;


import dev.squaremile.asynctcp.transport.api.values.DelineationType;

public class FixedLengthDelineationType implements DelineationType
{
    private final String name;
    private final int fixedLength;

    public FixedLengthDelineationType(final int fixedLength)
    {
        this("FIXED_LENGTH", fixedLength);
    }

    public FixedLengthDelineationType(final String name, final int fixedLength)
    {
        this.name = name;
        this.fixedLength = fixedLength;
    }

    @Override
    public int fixedLength()
    {
        return fixedLength;
    }

    @Override
    public String toString()
    {
        return "FixedLengthDelineationType{" +
               "name='" + name + '\'' +
               ", fixedLength=" + fixedLength +
               '}';
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
        final FixedLengthDelineationType that = (FixedLengthDelineationType)o;
        return fixedLength == that.fixedLength &&
               Objects.equals(name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, fixedLength);
    }
}

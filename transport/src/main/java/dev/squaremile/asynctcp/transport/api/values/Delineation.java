package dev.squaremile.asynctcp.transport.api.values;

import java.util.Objects;

public class Delineation
{
    private final Type type;
    private final int knownLength;
    private final String pattern;

    /**
     * Prescribes a delineation of data (how to turn stream into messages).
     *
     * <pre>
     *     // The pattern extracts the length of the data in bytes, when evaluated as follows
     *
     *     int lengthInBytes(final String data, final String pattern)
     *     {
     *         Matcher matcher = Pattern.compile(pattern).matcher(data);
     *         if (matcher.find()) {
     *             return Integer.parseInt(matcher.group(1));
     *         }
     *         else
     *         {
     *             throw new IllegalArgumentException();
     *         }
     *     }
     *
     *     // the total length is then calculated using the following formula
     *     int length = lengthInBytes(data,pattern) + offset of the extracted pattern + knownLength
     * </pre>
     *
     * @param type        Delineation type (e.g. fixed length or length extracted from an ascii pattern)
     * @param knownLength Either predefined length, or the length on top of what the pattern extracted
     * @param pattern     Describes how to extract the length in bytes, e.g. 8=[^\u0001]+\u00019=([0-9]+)\u0001
     */
    public Delineation(final Type type, final int knownLength, final String pattern)
    {
        this.type = type;
        this.knownLength = knownLength;
        this.pattern = pattern;
    }

    public int knownLength()
    {
        return knownLength;
    }

    public Type type()
    {
        return type;
    }

    /**
     * @return pattern that points to the ASCII-represented length
     */
    public String pattern()
    {
        return pattern;
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
        FIXED_LENGTH,
        ASCII_PATTERN
    }
}

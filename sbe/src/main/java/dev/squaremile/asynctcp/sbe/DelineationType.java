/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.asynctcp.sbe;

public enum DelineationType
{
    FIXED_LENGTH((byte)48),

    ASCII_PATTERN((byte)49),

    /**
     * To be used to represent not present or null.
     */
    NULL_VAL((byte)0);

    private final byte value;

    DelineationType(final byte value)
    {
        this.value = value;
    }

    public byte value()
    {
        return value;
    }

    public static DelineationType get(final byte value)
    {
        switch (value)
        {
            case 48: return FIXED_LENGTH;
            case 49: return ASCII_PATTERN;
            case 0: return NULL_VAL;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}

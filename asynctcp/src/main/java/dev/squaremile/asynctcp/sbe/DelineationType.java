/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.asynctcp.sbe;

public enum DelineationType
{
    ASCII_PATTERN((byte)48),

    FIXED_LENGTH((byte)49),

    SHORT_BIG_ENDIAN_FIELD((byte)50),

    SHORT_LITTLE_ENDIAN_FIELD((byte)51),

    INT_BIG_ENDIAN_FIELD((byte)52),

    INT_LITTLE_ENDIAN_FIELD((byte)53),

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
            case 48: return ASCII_PATTERN;
            case 49: return FIXED_LENGTH;
            case 50: return SHORT_BIG_ENDIAN_FIELD;
            case 51: return SHORT_LITTLE_ENDIAN_FIELD;
            case 52: return INT_BIG_ENDIAN_FIELD;
            case 53: return INT_LITTLE_ENDIAN_FIELD;
            case 0: return NULL_VAL;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}

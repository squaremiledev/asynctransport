/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.asynctcp.sbe;

import org.agrona.MutableDirectBuffer;

/**
 * Variable length ASCII String.
 */
@SuppressWarnings("all")
public class VarAsciiEncodingEncoder
{
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final int ENCODED_LENGTH = 4;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private int offset;
    private MutableDirectBuffer buffer;

    public VarAsciiEncodingEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;

        return this;
    }

    public MutableDirectBuffer buffer()
    {
        return buffer;
    }

    public int offset()
    {
        return offset;
    }

    public int encodedLength()
    {
        return ENCODED_LENGTH;
    }

    public int sbeSchemaId()
    {
        return SCHEMA_ID;
    }

    public int sbeSchemaVersion()
    {
        return SCHEMA_VERSION;
    }

    public static int lengthEncodingOffset()
    {
        return 0;
    }

    public static int lengthEncodingLength()
    {
        return 4;
    }

    public static long lengthNullValue()
    {
        return 4294967295L;
    }

    public static long lengthMinValue()
    {
        return 0L;
    }

    public static long lengthMaxValue()
    {
        return 1073741824L;
    }

    public VarAsciiEncodingEncoder length(final long value)
    {
        buffer.putInt(offset + 0, (int)value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int varDataEncodingOffset()
    {
        return 4;
    }

    public static int varDataEncodingLength()
    {
        return 0;
    }

    public static short varDataNullValue()
    {
        return (short)255;
    }

    public static short varDataMinValue()
    {
        return (short)0;
    }

    public static short varDataMaxValue()
    {
        return (short)254;
    }

    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        VarAsciiEncodingDecoder writer = new VarAsciiEncodingDecoder();
        writer.wrap(buffer, offset);

        return writer.appendTo(builder);
    }
}

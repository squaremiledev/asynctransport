/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.asynctcp.sbe;

import org.agrona.MutableDirectBuffer;
import org.agrona.DirectBuffer;

@SuppressWarnings("all")
public class ListenEncoder
{
    public static final int BLOCK_LENGTH = 12;
    public static final int TEMPLATE_ID = 103;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final ListenEncoder parentMessage = this;
    private MutableDirectBuffer buffer;
    protected int offset;
    protected int limit;

    public int sbeBlockLength()
    {
        return BLOCK_LENGTH;
    }

    public int sbeTemplateId()
    {
        return TEMPLATE_ID;
    }

    public int sbeSchemaId()
    {
        return SCHEMA_ID;
    }

    public int sbeSchemaVersion()
    {
        return SCHEMA_VERSION;
    }

    public String sbeSemanticType()
    {
        return "";
    }

    public MutableDirectBuffer buffer()
    {
        return buffer;
    }

    public int offset()
    {
        return offset;
    }

    public ListenEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;
        limit(offset + BLOCK_LENGTH);

        return this;
    }

    public ListenEncoder wrapAndApplyHeader(
        final MutableDirectBuffer buffer, final int offset, final MessageHeaderEncoder headerEncoder)
    {
        headerEncoder
            .wrap(buffer, offset)
            .blockLength(BLOCK_LENGTH)
            .templateId(TEMPLATE_ID)
            .schemaId(SCHEMA_ID)
            .version(SCHEMA_VERSION);

        return wrap(buffer, offset + MessageHeaderEncoder.ENCODED_LENGTH);
    }

    public int encodedLength()
    {
        return limit - offset;
    }

    public int limit()
    {
        return limit;
    }

    public void limit(final int limit)
    {
        this.limit = limit;
    }

    public static int portId()
    {
        return 1;
    }

    public static int portSinceVersion()
    {
        return 0;
    }

    public static int portEncodingOffset()
    {
        return 0;
    }

    public static int portEncodingLength()
    {
        return 4;
    }

    public static String portMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static int portNullValue()
    {
        return -2147483648;
    }

    public static int portMinValue()
    {
        return -2147483647;
    }

    public static int portMaxValue()
    {
        return 2147483647;
    }

    public ListenEncoder port(final int value)
    {
        buffer.putInt(offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int commandIdId()
    {
        return 2;
    }

    public static int commandIdSinceVersion()
    {
        return 0;
    }

    public static int commandIdEncodingOffset()
    {
        return 4;
    }

    public static int commandIdEncodingLength()
    {
        return 8;
    }

    public static String commandIdMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "";
            case TIME_UNIT: return "";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static long commandIdNullValue()
    {
        return -9223372036854775808L;
    }

    public static long commandIdMinValue()
    {
        return -9223372036854775807L;
    }

    public static long commandIdMaxValue()
    {
        return 9223372036854775807L;
    }

    public ListenEncoder commandId(final long value)
    {
        buffer.putLong(offset + 4, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int encodingId()
    {
        return 4;
    }

    public static String encodingCharacterEncoding()
    {
        return "ASCII";
    }

    public static String encodingMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH: return "unix";
            case TIME_UNIT: return "nanosecond";
            case SEMANTIC_TYPE: return "";
            case PRESENCE: return "required";
        }

        return "";
    }

    public static int encodingHeaderLength()
    {
        return 4;
    }

    public ListenEncoder putEncoding(final DirectBuffer src, final int srcOffset, final int length)
    {
        if (length > 1073741824)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 4;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putInt(limit, (int)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public ListenEncoder putEncoding(final byte[] src, final int srcOffset, final int length)
    {
        if (length > 1073741824)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 4;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putInt(limit, (int)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, src, srcOffset, length);

        return this;
    }

    public ListenEncoder encoding(final String value)
    {
        final int length = null == value ? 0 : value.length();
        if (length > 1073741824)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 4;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putInt(limit, (int)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putStringWithoutLengthAscii(limit + headerLength, value);

        return this;
    }

    public ListenEncoder encoding(final CharSequence value)
    {
        final int length = null == value ? 0 : value.length();
        if (length > 1073741824)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 4;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putInt(limit, (int)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < length; ++i)
        {
            final char charValue = value.charAt(i);
            final byte byteValue = charValue > 127 ? (byte)'?' : (byte)charValue;
            buffer.putByte(limit + headerLength + i, byteValue);
        }

        return this;
    }


    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        ListenDecoder writer = new ListenDecoder();
        writer.wrap(buffer, offset, BLOCK_LENGTH, SCHEMA_VERSION);

        return writer.appendTo(builder);
    }
}

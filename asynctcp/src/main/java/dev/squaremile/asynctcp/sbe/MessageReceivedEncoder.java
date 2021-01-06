/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.asynctcp.sbe;

import org.agrona.MutableDirectBuffer;
import org.agrona.DirectBuffer;

@SuppressWarnings("all")
public class MessageReceivedEncoder
{
    public static final int BLOCK_LENGTH = 16;
    public static final int TEMPLATE_ID = 8;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final MessageReceivedEncoder parentMessage = this;
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

    public MessageReceivedEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;
        limit(offset + BLOCK_LENGTH);

        return this;
    }

    public MessageReceivedEncoder wrapAndApplyHeader(
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

    public MessageReceivedEncoder port(final int value)
    {
        buffer.putInt(offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int connectionIdId()
    {
        return 2;
    }

    public static int connectionIdSinceVersion()
    {
        return 0;
    }

    public static int connectionIdEncodingOffset()
    {
        return 4;
    }

    public static int connectionIdEncodingLength()
    {
        return 8;
    }

    public static String connectionIdMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long connectionIdNullValue()
    {
        return -9223372036854775808L;
    }

    public static long connectionIdMinValue()
    {
        return -9223372036854775807L;
    }

    public static long connectionIdMaxValue()
    {
        return 9223372036854775807L;
    }

    public MessageReceivedEncoder connectionId(final long value)
    {
        buffer.putLong(offset + 4, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int dataId()
    {
        return 3;
    }

    public static int dataSinceVersion()
    {
        return 0;
    }

    public static int dataEncodingOffset()
    {
        return 12;
    }

    public static int dataEncodingLength()
    {
        return 4;
    }

    public static String dataMetaAttribute(final MetaAttribute metaAttribute)
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

    private final VarDataEncodingEncoder data = new VarDataEncodingEncoder();

    public VarDataEncodingEncoder data()
    {
        data.wrap(buffer, offset + 12);
        return data;
    }


    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        MessageReceivedDecoder writer = new MessageReceivedDecoder();
        writer.wrap(buffer, offset, BLOCK_LENGTH, SCHEMA_VERSION);

        return writer.appendTo(builder);
    }
}

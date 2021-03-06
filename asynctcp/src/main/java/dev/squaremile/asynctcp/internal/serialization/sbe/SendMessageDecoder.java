/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.asynctcp.internal.serialization.sbe;

import org.agrona.MutableDirectBuffer;
import org.agrona.DirectBuffer;

@SuppressWarnings("all")
public class SendMessageDecoder
{
    public static final int BLOCK_LENGTH = 24;
    public static final int TEMPLATE_ID = 106;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final SendMessageDecoder parentMessage = this;
    private DirectBuffer buffer;
    protected int offset;
    protected int limit;
    protected int actingBlockLength;
    protected int actingVersion;

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

    public DirectBuffer buffer()
    {
        return buffer;
    }

    public int offset()
    {
        return offset;
    }

    public SendMessageDecoder wrap(
        final DirectBuffer buffer,
        final int offset,
        final int actingBlockLength,
        final int actingVersion)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;
        this.actingBlockLength = actingBlockLength;
        this.actingVersion = actingVersion;
        limit(offset + actingBlockLength);

        return this;
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

    public int port()
    {
        return buffer.getInt(offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN);
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

    public long connectionId()
    {
        return buffer.getLong(offset + 4, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int commandIdId()
    {
        return 3;
    }

    public static int commandIdSinceVersion()
    {
        return 0;
    }

    public static int commandIdEncodingOffset()
    {
        return 12;
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

    public long commandId()
    {
        return buffer.getLong(offset + 12, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int dataId()
    {
        return 4;
    }

    public static int dataSinceVersion()
    {
        return 0;
    }

    public static int dataEncodingOffset()
    {
        return 20;
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

    private final VarDataEncodingDecoder data = new VarDataEncodingDecoder();

    public VarDataEncodingDecoder data()
    {
        data.wrap(buffer, offset + 20);
        return data;
    }


    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        final int originalLimit = limit();
        limit(offset + actingBlockLength);
        builder.append("[SendMessage](sbeTemplateId=");
        builder.append(TEMPLATE_ID);
        builder.append("|sbeSchemaId=");
        builder.append(SCHEMA_ID);
        builder.append("|sbeSchemaVersion=");
        if (parentMessage.actingVersion != SCHEMA_VERSION)
        {
            builder.append(parentMessage.actingVersion);
            builder.append('/');
        }
        builder.append(SCHEMA_VERSION);
        builder.append("|sbeBlockLength=");
        if (actingBlockLength != BLOCK_LENGTH)
        {
            builder.append(actingBlockLength);
            builder.append('/');
        }
        builder.append(BLOCK_LENGTH);
        builder.append("):");
        builder.append("port=");
        builder.append(port());
        builder.append('|');
        builder.append("connectionId=");
        builder.append(connectionId());
        builder.append('|');
        builder.append("commandId=");
        builder.append(commandId());
        builder.append('|');
        builder.append("data=");
        final VarDataEncodingDecoder data = data();
        if (data != null)
        {
            data.appendTo(builder);
        }
        else
        {
            builder.append("null");
        }

        limit(originalLimit);

        return builder;
    }
}

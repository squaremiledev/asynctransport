/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.asynctcp.sbe;

import org.agrona.MutableDirectBuffer;
import org.agrona.DirectBuffer;

@SuppressWarnings("all")
public class ConnectedDecoder
{
    public static final int BLOCK_LENGTH = 41;
    public static final int TEMPLATE_ID = 3;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final ConnectedDecoder parentMessage = this;
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

    public ConnectedDecoder wrap(
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

    public long commandId()
    {
        return buffer.getLong(offset + 4, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int connectionIdId()
    {
        return 3;
    }

    public static int connectionIdSinceVersion()
    {
        return 0;
    }

    public static int connectionIdEncodingOffset()
    {
        return 12;
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
        return buffer.getLong(offset + 12, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int remotePortId()
    {
        return 4;
    }

    public static int remotePortSinceVersion()
    {
        return 0;
    }

    public static int remotePortEncodingOffset()
    {
        return 20;
    }

    public static int remotePortEncodingLength()
    {
        return 4;
    }

    public static String remotePortMetaAttribute(final MetaAttribute metaAttribute)
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

    public static int remotePortNullValue()
    {
        return -2147483648;
    }

    public static int remotePortMinValue()
    {
        return -2147483647;
    }

    public static int remotePortMaxValue()
    {
        return 2147483647;
    }

    public int remotePort()
    {
        return buffer.getInt(offset + 20, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int inboundPduLimitId()
    {
        return 5;
    }

    public static int inboundPduLimitSinceVersion()
    {
        return 0;
    }

    public static int inboundPduLimitEncodingOffset()
    {
        return 24;
    }

    public static int inboundPduLimitEncodingLength()
    {
        return 4;
    }

    public static String inboundPduLimitMetaAttribute(final MetaAttribute metaAttribute)
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

    public static int inboundPduLimitNullValue()
    {
        return -2147483648;
    }

    public static int inboundPduLimitMinValue()
    {
        return -2147483647;
    }

    public static int inboundPduLimitMaxValue()
    {
        return 2147483647;
    }

    public int inboundPduLimit()
    {
        return buffer.getInt(offset + 24, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int outboundPduLimitId()
    {
        return 6;
    }

    public static int outboundPduLimitSinceVersion()
    {
        return 0;
    }

    public static int outboundPduLimitEncodingOffset()
    {
        return 28;
    }

    public static int outboundPduLimitEncodingLength()
    {
        return 4;
    }

    public static String outboundPduLimitMetaAttribute(final MetaAttribute metaAttribute)
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

    public static int outboundPduLimitNullValue()
    {
        return -2147483648;
    }

    public static int outboundPduLimitMinValue()
    {
        return -2147483647;
    }

    public static int outboundPduLimitMaxValue()
    {
        return 2147483647;
    }

    public int outboundPduLimit()
    {
        return buffer.getInt(offset + 28, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int delineationTypeId()
    {
        return 7;
    }

    public static int delineationTypeSinceVersion()
    {
        return 0;
    }

    public static int delineationTypeEncodingOffset()
    {
        return 32;
    }

    public static int delineationTypeEncodingLength()
    {
        return 1;
    }

    public static String delineationTypeMetaAttribute(final MetaAttribute metaAttribute)
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

    public DelineationType delineationType()
    {
        return DelineationType.get(buffer.getByte(offset + 32));
    }


    public static int delineationPaddingId()
    {
        return 8;
    }

    public static int delineationPaddingSinceVersion()
    {
        return 0;
    }

    public static int delineationPaddingEncodingOffset()
    {
        return 33;
    }

    public static int delineationPaddingEncodingLength()
    {
        return 4;
    }

    public static String delineationPaddingMetaAttribute(final MetaAttribute metaAttribute)
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

    public static int delineationPaddingNullValue()
    {
        return -2147483648;
    }

    public static int delineationPaddingMinValue()
    {
        return -2147483647;
    }

    public static int delineationPaddingMaxValue()
    {
        return 2147483647;
    }

    public int delineationPadding()
    {
        return buffer.getInt(offset + 33, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int delineationKnownLengthId()
    {
        return 9;
    }

    public static int delineationKnownLengthSinceVersion()
    {
        return 0;
    }

    public static int delineationKnownLengthEncodingOffset()
    {
        return 37;
    }

    public static int delineationKnownLengthEncodingLength()
    {
        return 4;
    }

    public static String delineationKnownLengthMetaAttribute(final MetaAttribute metaAttribute)
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

    public static int delineationKnownLengthNullValue()
    {
        return -2147483648;
    }

    public static int delineationKnownLengthMinValue()
    {
        return -2147483647;
    }

    public static int delineationKnownLengthMaxValue()
    {
        return 2147483647;
    }

    public int delineationKnownLength()
    {
        return buffer.getInt(offset + 37, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int delineationPatternId()
    {
        return 10;
    }

    public static int delineationPatternSinceVersion()
    {
        return 0;
    }

    public static String delineationPatternCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String delineationPatternMetaAttribute(final MetaAttribute metaAttribute)
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

    public static int delineationPatternHeaderLength()
    {
        return 4;
    }

    public int delineationPatternLength()
    {
        final int limit = parentMessage.limit();
        return (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
    }

    public int skipDelineationPattern()
    {
        final int headerLength = 4;
        final int limit = parentMessage.limit();
        final int dataLength = (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
        final int dataOffset = limit + headerLength;

        parentMessage.limit(dataOffset + dataLength);

        return dataLength;
    }

    public int getDelineationPattern(final MutableDirectBuffer dst, final int dstOffset, final int length)
    {
        final int headerLength = 4;
        final int limit = parentMessage.limit();
        final int dataLength = (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int getDelineationPattern(final byte[] dst, final int dstOffset, final int length)
    {
        final int headerLength = 4;
        final int limit = parentMessage.limit();
        final int dataLength = (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public void wrapDelineationPattern(final DirectBuffer wrapBuffer)
    {
        final int headerLength = 4;
        final int limit = parentMessage.limit();
        final int dataLength = (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
        parentMessage.limit(limit + headerLength + dataLength);
        wrapBuffer.wrap(buffer, limit + headerLength, dataLength);
    }

    public String delineationPattern()
    {
        final int headerLength = 4;
        final int limit = parentMessage.limit();
        final int dataLength = (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
        parentMessage.limit(limit + headerLength + dataLength);

        if (0 == dataLength)
        {
            return "";
        }

        final byte[] tmp = new byte[dataLength];
        buffer.getBytes(limit + headerLength, tmp, 0, dataLength);

        final String value;
        try
        {
            value = new String(tmp, "UTF-8");
        }
        catch (final java.io.UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }

        return value;
    }

    public static int remoteHostId()
    {
        return 11;
    }

    public static int remoteHostSinceVersion()
    {
        return 0;
    }

    public static String remoteHostCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String remoteHostMetaAttribute(final MetaAttribute metaAttribute)
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

    public static int remoteHostHeaderLength()
    {
        return 4;
    }

    public int remoteHostLength()
    {
        final int limit = parentMessage.limit();
        return (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
    }

    public int skipRemoteHost()
    {
        final int headerLength = 4;
        final int limit = parentMessage.limit();
        final int dataLength = (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
        final int dataOffset = limit + headerLength;

        parentMessage.limit(dataOffset + dataLength);

        return dataLength;
    }

    public int getRemoteHost(final MutableDirectBuffer dst, final int dstOffset, final int length)
    {
        final int headerLength = 4;
        final int limit = parentMessage.limit();
        final int dataLength = (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public int getRemoteHost(final byte[] dst, final int dstOffset, final int length)
    {
        final int headerLength = 4;
        final int limit = parentMessage.limit();
        final int dataLength = (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
        final int bytesCopied = Math.min(length, dataLength);
        parentMessage.limit(limit + headerLength + dataLength);
        buffer.getBytes(limit + headerLength, dst, dstOffset, bytesCopied);

        return bytesCopied;
    }

    public void wrapRemoteHost(final DirectBuffer wrapBuffer)
    {
        final int headerLength = 4;
        final int limit = parentMessage.limit();
        final int dataLength = (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
        parentMessage.limit(limit + headerLength + dataLength);
        wrapBuffer.wrap(buffer, limit + headerLength, dataLength);
    }

    public String remoteHost()
    {
        final int headerLength = 4;
        final int limit = parentMessage.limit();
        final int dataLength = (int)(buffer.getInt(limit, java.nio.ByteOrder.LITTLE_ENDIAN) & 0xFFFF_FFFFL);
        parentMessage.limit(limit + headerLength + dataLength);

        if (0 == dataLength)
        {
            return "";
        }

        final byte[] tmp = new byte[dataLength];
        buffer.getBytes(limit + headerLength, tmp, 0, dataLength);

        final String value;
        try
        {
            value = new String(tmp, "UTF-8");
        }
        catch (final java.io.UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }

        return value;
    }


    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        final int originalLimit = limit();
        limit(offset + actingBlockLength);
        builder.append("[Connected](sbeTemplateId=");
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
        builder.append("commandId=");
        builder.append(commandId());
        builder.append('|');
        builder.append("connectionId=");
        builder.append(connectionId());
        builder.append('|');
        builder.append("remotePort=");
        builder.append(remotePort());
        builder.append('|');
        builder.append("inboundPduLimit=");
        builder.append(inboundPduLimit());
        builder.append('|');
        builder.append("outboundPduLimit=");
        builder.append(outboundPduLimit());
        builder.append('|');
        builder.append("delineationType=");
        builder.append(delineationType());
        builder.append('|');
        builder.append("delineationPadding=");
        builder.append(delineationPadding());
        builder.append('|');
        builder.append("delineationKnownLength=");
        builder.append(delineationKnownLength());
        builder.append('|');
        builder.append("delineationPattern=");
        builder.append('\'').append(delineationPattern()).append('\'');
        builder.append('|');
        builder.append("remoteHost=");
        builder.append('\'').append(remoteHost()).append('\'');

        limit(originalLimit);

        return builder;
    }
}

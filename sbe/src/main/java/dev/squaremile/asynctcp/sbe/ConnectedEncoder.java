/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.asynctcp.sbe;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

@SuppressWarnings("all")
public class ConnectedEncoder
{
    public static final int BLOCK_LENGTH = 32;
    public static final int TEMPLATE_ID = 3;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final ConnectedEncoder parentMessage = this;
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

    public ConnectedEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;
        limit(offset + BLOCK_LENGTH);

        return this;
    }

    public ConnectedEncoder wrapAndApplyHeader(
            final MutableDirectBuffer buffer, final int offset, final MessageHeaderEncoder headerEncoder
    )
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
            case EPOCH:
                return "";
            case TIME_UNIT:
                return "";
            case SEMANTIC_TYPE:
                return "";
            case PRESENCE:
                return "required";
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

    public ConnectedEncoder port(final int value)
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
            case EPOCH:
                return "";
            case TIME_UNIT:
                return "";
            case SEMANTIC_TYPE:
                return "";
            case PRESENCE:
                return "required";
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

    public ConnectedEncoder commandId(final long value)
    {
        buffer.putLong(offset + 4, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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
            case EPOCH:
                return "";
            case TIME_UNIT:
                return "";
            case SEMANTIC_TYPE:
                return "";
            case PRESENCE:
                return "required";
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

    public ConnectedEncoder connectionId(final long value)
    {
        buffer.putLong(offset + 12, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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
            case EPOCH:
                return "";
            case TIME_UNIT:
                return "";
            case SEMANTIC_TYPE:
                return "";
            case PRESENCE:
                return "required";
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

    public ConnectedEncoder remotePort(final int value)
    {
        buffer.putInt(offset + 20, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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
            case EPOCH:
                return "";
            case TIME_UNIT:
                return "";
            case SEMANTIC_TYPE:
                return "";
            case PRESENCE:
                return "required";
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

    public ConnectedEncoder inboundPduLimit(final int value)
    {
        buffer.putInt(offset + 24, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
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
            case EPOCH:
                return "";
            case TIME_UNIT:
                return "";
            case SEMANTIC_TYPE:
                return "";
            case PRESENCE:
                return "required";
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

    public ConnectedEncoder outboundPduLimit(final int value)
    {
        buffer.putInt(offset + 28, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int remoteHostId()
    {
        return 7;
    }

    public static String remoteHostCharacterEncoding()
    {
        return "UTF-8";
    }

    public static String remoteHostMetaAttribute(final MetaAttribute metaAttribute)
    {
        switch (metaAttribute)
        {
            case EPOCH:
                return "unix";
            case TIME_UNIT:
                return "nanosecond";
            case SEMANTIC_TYPE:
                return "";
            case PRESENCE:
                return "required";
        }

        return "";
    }

    public static int remoteHostHeaderLength()
    {
        return 4;
    }

    public ConnectedEncoder putRemoteHost(final DirectBuffer src, final int srcOffset, final int length)
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

    public ConnectedEncoder putRemoteHost(final byte[] src, final int srcOffset, final int length)
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

    public ConnectedEncoder remoteHost(final String value)
    {
        final byte[] bytes;
        try
        {
            bytes = null == value || value.isEmpty() ? org.agrona.collections.ArrayUtil.EMPTY_BYTE_ARRAY : value.getBytes("UTF-8");
        }
        catch (final java.io.UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }

        final int length = bytes.length;
        if (length > 1073741824)
        {
            throw new IllegalStateException("length > maxValue for type: " + length);
        }

        final int headerLength = 4;
        final int limit = parentMessage.limit();
        parentMessage.limit(limit + headerLength + length);
        buffer.putInt(limit, (int)length, java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putBytes(limit + headerLength, bytes, 0, length);

        return this;
    }


    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        ConnectedDecoder writer = new ConnectedDecoder();
        writer.wrap(buffer, offset, BLOCK_LENGTH, SCHEMA_VERSION);

        return writer.appendTo(builder);
    }
}

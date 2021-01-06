/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.asynctcp.sbe;

import org.agrona.MutableDirectBuffer;
import org.agrona.DirectBuffer;

@SuppressWarnings("all")
public class ConnectEncoder
{
    public static final int BLOCK_LENGTH = 25;
    public static final int TEMPLATE_ID = 102;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final ConnectEncoder parentMessage = this;
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

    public ConnectEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;
        limit(offset + BLOCK_LENGTH);

        return this;
    }

    public ConnectEncoder wrapAndApplyHeader(
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

    public static int remotePortId()
    {
        return 1;
    }

    public static int remotePortSinceVersion()
    {
        return 0;
    }

    public static int remotePortEncodingOffset()
    {
        return 0;
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

    public ConnectEncoder remotePort(final int value)
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

    public ConnectEncoder commandId(final long value)
    {
        buffer.putLong(offset + 4, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int timeoutMsId()
    {
        return 3;
    }

    public static int timeoutMsSinceVersion()
    {
        return 0;
    }

    public static int timeoutMsEncodingOffset()
    {
        return 12;
    }

    public static int timeoutMsEncodingLength()
    {
        return 4;
    }

    public static String timeoutMsMetaAttribute(final MetaAttribute metaAttribute)
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

    public static int timeoutMsNullValue()
    {
        return -2147483648;
    }

    public static int timeoutMsMinValue()
    {
        return -2147483647;
    }

    public static int timeoutMsMaxValue()
    {
        return 2147483647;
    }

    public ConnectEncoder timeoutMs(final int value)
    {
        buffer.putInt(offset + 12, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int delineationTypeId()
    {
        return 4;
    }

    public static int delineationTypeSinceVersion()
    {
        return 0;
    }

    public static int delineationTypeEncodingOffset()
    {
        return 16;
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

    public ConnectEncoder delineationType(final DelineationType value)
    {
        buffer.putByte(offset + 16, value.value());
        return this;
    }

    public static int delineationPaddingId()
    {
        return 5;
    }

    public static int delineationPaddingSinceVersion()
    {
        return 0;
    }

    public static int delineationPaddingEncodingOffset()
    {
        return 17;
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

    public ConnectEncoder delineationPadding(final int value)
    {
        buffer.putInt(offset + 17, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int delineationKnownLengthId()
    {
        return 6;
    }

    public static int delineationKnownLengthSinceVersion()
    {
        return 0;
    }

    public static int delineationKnownLengthEncodingOffset()
    {
        return 21;
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

    public ConnectEncoder delineationKnownLength(final int value)
    {
        buffer.putInt(offset + 21, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int delineationPatternId()
    {
        return 7;
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

    public ConnectEncoder putDelineationPattern(final DirectBuffer src, final int srcOffset, final int length)
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

    public ConnectEncoder putDelineationPattern(final byte[] src, final int srcOffset, final int length)
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

    public ConnectEncoder delineationPattern(final String value)
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

    public static int remoteHostId()
    {
        return 8;
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

    public ConnectEncoder putRemoteHost(final DirectBuffer src, final int srcOffset, final int length)
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

    public ConnectEncoder putRemoteHost(final byte[] src, final int srcOffset, final int length)
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

    public ConnectEncoder remoteHost(final String value)
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
        ConnectDecoder writer = new ConnectDecoder();
        writer.wrap(buffer, offset, BLOCK_LENGTH, SCHEMA_VERSION);

        return writer.appendTo(builder);
    }
}

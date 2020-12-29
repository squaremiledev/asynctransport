/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.transport.casestudy.marketmaking.schema;

import org.agrona.MutableDirectBuffer;
import org.agrona.DirectBuffer;

@SuppressWarnings("all")
public class FirmPriceEncoder
{
    public static final int BLOCK_LENGTH = 40;
    public static final int TEMPLATE_ID = 1;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final FirmPriceEncoder parentMessage = this;
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

    public FirmPriceEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;
        limit(offset + BLOCK_LENGTH);

        return this;
    }

    public FirmPriceEncoder wrapAndApplyHeader(
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

    public static int correlationIdId()
    {
        return 1;
    }

    public static int correlationIdSinceVersion()
    {
        return 0;
    }

    public static int correlationIdEncodingOffset()
    {
        return 0;
    }

    public static int correlationIdEncodingLength()
    {
        return 8;
    }

    public static String correlationIdMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long correlationIdNullValue()
    {
        return -9223372036854775808L;
    }

    public static long correlationIdMinValue()
    {
        return -9223372036854775807L;
    }

    public static long correlationIdMaxValue()
    {
        return 9223372036854775807L;
    }

    public FirmPriceEncoder correlationId(final long value)
    {
        buffer.putLong(offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int updateTimeId()
    {
        return 2;
    }

    public static int updateTimeSinceVersion()
    {
        return 0;
    }

    public static int updateTimeEncodingOffset()
    {
        return 8;
    }

    public static int updateTimeEncodingLength()
    {
        return 8;
    }

    public static String updateTimeMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long updateTimeNullValue()
    {
        return -9223372036854775808L;
    }

    public static long updateTimeMinValue()
    {
        return -9223372036854775807L;
    }

    public static long updateTimeMaxValue()
    {
        return 9223372036854775807L;
    }

    public FirmPriceEncoder updateTime(final long value)
    {
        buffer.putLong(offset + 8, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int bidPriceId()
    {
        return 3;
    }

    public static int bidPriceSinceVersion()
    {
        return 0;
    }

    public static int bidPriceEncodingOffset()
    {
        return 16;
    }

    public static int bidPriceEncodingLength()
    {
        return 8;
    }

    public static String bidPriceMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long bidPriceNullValue()
    {
        return -9223372036854775808L;
    }

    public static long bidPriceMinValue()
    {
        return -9223372036854775807L;
    }

    public static long bidPriceMaxValue()
    {
        return 9223372036854775807L;
    }

    public FirmPriceEncoder bidPrice(final long value)
    {
        buffer.putLong(offset + 16, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int bidQuantityId()
    {
        return 4;
    }

    public static int bidQuantitySinceVersion()
    {
        return 0;
    }

    public static int bidQuantityEncodingOffset()
    {
        return 24;
    }

    public static int bidQuantityEncodingLength()
    {
        return 4;
    }

    public static String bidQuantityMetaAttribute(final MetaAttribute metaAttribute)
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

    public static int bidQuantityNullValue()
    {
        return -2147483648;
    }

    public static int bidQuantityMinValue()
    {
        return -2147483647;
    }

    public static int bidQuantityMaxValue()
    {
        return 2147483647;
    }

    public FirmPriceEncoder bidQuantity(final int value)
    {
        buffer.putInt(offset + 24, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int askPriceId()
    {
        return 5;
    }

    public static int askPriceSinceVersion()
    {
        return 0;
    }

    public static int askPriceEncodingOffset()
    {
        return 28;
    }

    public static int askPriceEncodingLength()
    {
        return 8;
    }

    public static String askPriceMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long askPriceNullValue()
    {
        return -9223372036854775808L;
    }

    public static long askPriceMinValue()
    {
        return -9223372036854775807L;
    }

    public static long askPriceMaxValue()
    {
        return 9223372036854775807L;
    }

    public FirmPriceEncoder askPrice(final long value)
    {
        buffer.putLong(offset + 28, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int askQuantityId()
    {
        return 6;
    }

    public static int askQuantitySinceVersion()
    {
        return 0;
    }

    public static int askQuantityEncodingOffset()
    {
        return 36;
    }

    public static int askQuantityEncodingLength()
    {
        return 4;
    }

    public static String askQuantityMetaAttribute(final MetaAttribute metaAttribute)
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

    public static int askQuantityNullValue()
    {
        return -2147483648;
    }

    public static int askQuantityMinValue()
    {
        return -2147483647;
    }

    public static int askQuantityMaxValue()
    {
        return 2147483647;
    }

    public FirmPriceEncoder askQuantity(final int value)
    {
        buffer.putInt(offset + 36, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }



    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        FirmPriceDecoder writer = new FirmPriceDecoder();
        writer.wrap(buffer, offset, BLOCK_LENGTH, SCHEMA_VERSION);

        return writer.appendTo(builder);
    }
}

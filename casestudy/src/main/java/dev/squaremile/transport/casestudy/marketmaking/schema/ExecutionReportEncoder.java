/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.transport.casestudy.marketmaking.schema;

import org.agrona.MutableDirectBuffer;
import org.agrona.DirectBuffer;

@SuppressWarnings("all")
public class ExecutionReportEncoder
{
    public static final int BLOCK_LENGTH = 56;
    public static final int TEMPLATE_ID = 4;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final ExecutionReportEncoder parentMessage = this;
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

    public ExecutionReportEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;
        limit(offset + BLOCK_LENGTH);

        return this;
    }

    public ExecutionReportEncoder wrapAndApplyHeader(
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

    public static int passiveMarketParticipantIdId()
    {
        return 1;
    }

    public static int passiveMarketParticipantIdSinceVersion()
    {
        return 0;
    }

    public static int passiveMarketParticipantIdEncodingOffset()
    {
        return 0;
    }

    public static int passiveMarketParticipantIdEncodingLength()
    {
        return 4;
    }

    public static String passiveMarketParticipantIdMetaAttribute(final MetaAttribute metaAttribute)
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

    public static int passiveMarketParticipantIdNullValue()
    {
        return -2147483648;
    }

    public static int passiveMarketParticipantIdMinValue()
    {
        return -2147483647;
    }

    public static int passiveMarketParticipantIdMaxValue()
    {
        return 2147483647;
    }

    public ExecutionReportEncoder passiveMarketParticipantId(final int value)
    {
        buffer.putInt(offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int aggressiveMarketParticipantIdId()
    {
        return 2;
    }

    public static int aggressiveMarketParticipantIdSinceVersion()
    {
        return 0;
    }

    public static int aggressiveMarketParticipantIdEncodingOffset()
    {
        return 4;
    }

    public static int aggressiveMarketParticipantIdEncodingLength()
    {
        return 4;
    }

    public static String aggressiveMarketParticipantIdMetaAttribute(final MetaAttribute metaAttribute)
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

    public static int aggressiveMarketParticipantIdNullValue()
    {
        return -2147483648;
    }

    public static int aggressiveMarketParticipantIdMinValue()
    {
        return -2147483647;
    }

    public static int aggressiveMarketParticipantIdMaxValue()
    {
        return 2147483647;
    }

    public ExecutionReportEncoder aggressiveMarketParticipantId(final int value)
    {
        buffer.putInt(offset + 4, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int midPriceId()
    {
        return 3;
    }

    public static int midPriceSinceVersion()
    {
        return 0;
    }

    public static int midPriceEncodingOffset()
    {
        return 8;
    }

    public static int midPriceEncodingLength()
    {
        return 8;
    }

    public static String midPriceMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long midPriceNullValue()
    {
        return -9223372036854775808L;
    }

    public static long midPriceMinValue()
    {
        return -9223372036854775807L;
    }

    public static long midPriceMaxValue()
    {
        return 9223372036854775807L;
    }

    public ExecutionReportEncoder midPrice(final long value)
    {
        buffer.putLong(offset + 8, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int lastUpdateTimeId()
    {
        return 4;
    }

    public static int lastUpdateTimeSinceVersion()
    {
        return 0;
    }

    public static int lastUpdateTimeEncodingOffset()
    {
        return 16;
    }

    public static int lastUpdateTimeEncodingLength()
    {
        return 8;
    }

    public static String lastUpdateTimeMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long lastUpdateTimeNullValue()
    {
        return -9223372036854775808L;
    }

    public static long lastUpdateTimeMinValue()
    {
        return -9223372036854775807L;
    }

    public static long lastUpdateTimeMaxValue()
    {
        return 9223372036854775807L;
    }

    public ExecutionReportEncoder lastUpdateTime(final long value)
    {
        buffer.putLong(offset + 16, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int lastPriceChangeId()
    {
        return 5;
    }

    public static int lastPriceChangeSinceVersion()
    {
        return 0;
    }

    public static int lastPriceChangeEncodingOffset()
    {
        return 24;
    }

    public static int lastPriceChangeEncodingLength()
    {
        return 8;
    }

    public static String lastPriceChangeMetaAttribute(final MetaAttribute metaAttribute)
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

    public static long lastPriceChangeNullValue()
    {
        return -9223372036854775808L;
    }

    public static long lastPriceChangeMinValue()
    {
        return -9223372036854775807L;
    }

    public static long lastPriceChangeMaxValue()
    {
        return 9223372036854775807L;
    }

    public ExecutionReportEncoder lastPriceChange(final long value)
    {
        buffer.putLong(offset + 24, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int bidPriceId()
    {
        return 6;
    }

    public static int bidPriceSinceVersion()
    {
        return 0;
    }

    public static int bidPriceEncodingOffset()
    {
        return 32;
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

    public ExecutionReportEncoder bidPrice(final long value)
    {
        buffer.putLong(offset + 32, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int bidQuantityId()
    {
        return 7;
    }

    public static int bidQuantitySinceVersion()
    {
        return 0;
    }

    public static int bidQuantityEncodingOffset()
    {
        return 40;
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

    public ExecutionReportEncoder bidQuantity(final int value)
    {
        buffer.putInt(offset + 40, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int askPriceId()
    {
        return 8;
    }

    public static int askPriceSinceVersion()
    {
        return 0;
    }

    public static int askPriceEncodingOffset()
    {
        return 44;
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

    public ExecutionReportEncoder askPrice(final long value)
    {
        buffer.putLong(offset + 44, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int askQuantityId()
    {
        return 9;
    }

    public static int askQuantitySinceVersion()
    {
        return 0;
    }

    public static int askQuantityEncodingOffset()
    {
        return 52;
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

    public ExecutionReportEncoder askQuantity(final int value)
    {
        buffer.putInt(offset + 52, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }



    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        ExecutionReportDecoder writer = new ExecutionReportDecoder();
        writer.wrap(buffer, offset, BLOCK_LENGTH, SCHEMA_VERSION);

        return writer.appendTo(builder);
    }
}

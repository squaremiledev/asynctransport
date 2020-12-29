/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.transport.casestudy.marketmaking.schema;

import org.agrona.MutableDirectBuffer;
import org.agrona.DirectBuffer;

@SuppressWarnings("all")
public class ExecutionReportDecoder
{
    public static final int BLOCK_LENGTH = 56;
    public static final int TEMPLATE_ID = 4;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final ExecutionReportDecoder parentMessage = this;
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

    public ExecutionReportDecoder wrap(
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

    public int passiveMarketParticipantId()
    {
        return buffer.getInt(offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN);
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

    public int aggressiveMarketParticipantId()
    {
        return buffer.getInt(offset + 4, java.nio.ByteOrder.LITTLE_ENDIAN);
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

    public long midPrice()
    {
        return buffer.getLong(offset + 8, java.nio.ByteOrder.LITTLE_ENDIAN);
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

    public long lastUpdateTime()
    {
        return buffer.getLong(offset + 16, java.nio.ByteOrder.LITTLE_ENDIAN);
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

    public long lastPriceChange()
    {
        return buffer.getLong(offset + 24, java.nio.ByteOrder.LITTLE_ENDIAN);
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

    public long bidPrice()
    {
        return buffer.getLong(offset + 32, java.nio.ByteOrder.LITTLE_ENDIAN);
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

    public int bidQuantity()
    {
        return buffer.getInt(offset + 40, java.nio.ByteOrder.LITTLE_ENDIAN);
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

    public long askPrice()
    {
        return buffer.getLong(offset + 44, java.nio.ByteOrder.LITTLE_ENDIAN);
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

    public int askQuantity()
    {
        return buffer.getInt(offset + 52, java.nio.ByteOrder.LITTLE_ENDIAN);
    }



    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        final int originalLimit = limit();
        limit(offset + actingBlockLength);
        builder.append("[ExecutionReport](sbeTemplateId=");
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
        builder.append("passiveMarketParticipantId=");
        builder.append(passiveMarketParticipantId());
        builder.append('|');
        builder.append("aggressiveMarketParticipantId=");
        builder.append(aggressiveMarketParticipantId());
        builder.append('|');
        builder.append("midPrice=");
        builder.append(midPrice());
        builder.append('|');
        builder.append("lastUpdateTime=");
        builder.append(lastUpdateTime());
        builder.append('|');
        builder.append("lastPriceChange=");
        builder.append(lastPriceChange());
        builder.append('|');
        builder.append("bidPrice=");
        builder.append(bidPrice());
        builder.append('|');
        builder.append("bidQuantity=");
        builder.append(bidQuantity());
        builder.append('|');
        builder.append("askPrice=");
        builder.append(askPrice());
        builder.append('|');
        builder.append("askQuantity=");
        builder.append(askQuantity());

        limit(originalLimit);

        return builder;
    }
}

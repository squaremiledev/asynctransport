/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.transport.casestudy.marketmaking.schema;

import org.agrona.MutableDirectBuffer;
import org.agrona.DirectBuffer;

@SuppressWarnings("all")
public class SecurityDecoder
{
    public static final int BLOCK_LENGTH = 24;
    public static final int TEMPLATE_ID = 5;
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private final SecurityDecoder parentMessage = this;
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

    public SecurityDecoder wrap(
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

    public static int midPriceId()
    {
        return 1;
    }

    public static int midPriceSinceVersion()
    {
        return 0;
    }

    public static int midPriceEncodingOffset()
    {
        return 0;
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
        return buffer.getLong(offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int lastUpdateTimeId()
    {
        return 2;
    }

    public static int lastUpdateTimeSinceVersion()
    {
        return 0;
    }

    public static int lastUpdateTimeEncodingOffset()
    {
        return 8;
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
        return buffer.getLong(offset + 8, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int lastPriceChangeId()
    {
        return 3;
    }

    public static int lastPriceChangeSinceVersion()
    {
        return 0;
    }

    public static int lastPriceChangeEncodingOffset()
    {
        return 16;
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
        return buffer.getLong(offset + 16, java.nio.ByteOrder.LITTLE_ENDIAN);
    }



    public String toString()
    {
        return appendTo(new StringBuilder(100)).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        final int originalLimit = limit();
        limit(offset + actingBlockLength);
        builder.append("[Security](sbeTemplateId=");
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
        builder.append("midPrice=");
        builder.append(midPrice());
        builder.append('|');
        builder.append("lastUpdateTime=");
        builder.append(lastUpdateTime());
        builder.append('|');
        builder.append("lastPriceChange=");
        builder.append(lastPriceChange());

        limit(originalLimit);

        return builder;
    }
}

package dev.squaremile.asynctcp.serialization.api.delineation;

import dev.squaremile.asynctcp.transport.api.values.DelineationType;

public enum PredefinedTransportDelineation
{
    RAW_STREAMING(new FixedLengthDelineationType(0)),
    SINGLE_BYTE(new FixedLengthDelineationType(1)),
    INTEGERS(new FixedLengthDelineationType(4)),
    LONGS(new FixedLengthDelineationType(8)),
    FOUR_KB(new FixedLengthDelineationType(4 * 1024));

    public final DelineationType type;

    PredefinedTransportDelineation(final DelineationType type)
    {
        this.type = type;
    }
}

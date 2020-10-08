package dev.squaremile.asynctcp.serialization.api;

import dev.squaremile.asynctcp.transport.api.values.Delineation;

import static dev.squaremile.asynctcp.transport.api.values.Delineation.Type.FIXED_LENGTH;

public enum PredefinedTransportDelineation
{
    RAW_STREAMING(new Delineation(FIXED_LENGTH, 0)),
    SINGLE_BYTE(new Delineation(FIXED_LENGTH, 1)),
    INTEGERS(new Delineation(FIXED_LENGTH, 4)),
    LONGS(new Delineation(FIXED_LENGTH, 8)),
    FOUR_KB(new Delineation(FIXED_LENGTH, 4 * 1024));

    public final Delineation type;

    PredefinedTransportDelineation(final Delineation type)
    {
        this.type = type;
    }
}

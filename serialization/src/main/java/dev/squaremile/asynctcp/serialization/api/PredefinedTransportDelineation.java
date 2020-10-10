package dev.squaremile.asynctcp.serialization.api;

import dev.squaremile.asynctcp.transport.api.values.Delineation;

import static dev.squaremile.asynctcp.transport.api.values.Delineation.fixedLengthDelineation;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.patternBasedLengthDelineation;

public enum PredefinedTransportDelineation
{
    RAW_STREAMING(fixedLengthDelineation(0)),
    SINGLE_BYTE(fixedLengthDelineation(1)),
    INTEGERS(fixedLengthDelineation(4)),
    LONGS(fixedLengthDelineation(8)),
    FOUR_KB(fixedLengthDelineation(4 * 1024)),
    FIX_MESSAGES(patternBasedLengthDelineation("8=[^\\u0001]+\\u00019=([0-9]+)\\u0001", 7 /*additional checksum size*/));

    public final Delineation type;

    PredefinedTransportDelineation(final Delineation type)
    {
        this.type = type;
    }
}

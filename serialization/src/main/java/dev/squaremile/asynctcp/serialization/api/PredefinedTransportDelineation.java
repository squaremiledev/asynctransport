package dev.squaremile.asynctcp.serialization.api;

import dev.squaremile.asynctcp.transport.api.values.Delineation;

public class PredefinedTransportDelineation
{
    public static Delineation rawStreaming()
    {
        return new Delineation(Delineation.Type.FIXED_LENGTH, 0, 0, "");
    }

    public static Delineation fixedLengthDelineation(final int length)
    {
        return new Delineation(Delineation.Type.FIXED_LENGTH, 0, length, "");
    }

    public static Delineation lengthBasedDelineation(final Delineation.Type type, final int padding, final int length)
    {
        return new Delineation(type, padding, length, "");
    }

    public static Delineation fixMessage()
    {
        return new Delineation(Delineation.Type.ASCII_PATTERN, 0, 7 /*additional checksum size*/, "8=[^\\u0001]+\\u00019=([0-9]+)\\u0001");
    }
}

package dev.squaremile.asynctcp.internal.serialization;

import dev.squaremile.asynctcp.api.transport.values.Delineation;
import dev.squaremile.asynctcp.internal.serialization.sbe.DelineationType;

public class DelineationTypeMapping
{
    public static Delineation.Type toDomain(final DelineationType wire)
    {
        switch (wire)
        {
            case FIXED_LENGTH:
                return Delineation.Type.FIXED_LENGTH;
            case ASCII_PATTERN:
                return Delineation.Type.ASCII_PATTERN;
            case SHORT_BIG_ENDIAN_FIELD:
                return Delineation.Type.SHORT_BIG_ENDIAN_FIELD;
            case SHORT_LITTLE_ENDIAN_FIELD:
                return Delineation.Type.SHORT_LITTLE_ENDIAN_FIELD;
            case INT_BIG_ENDIAN_FIELD:
                return Delineation.Type.INT_BIG_ENDIAN_FIELD;
            case INT_LITTLE_ENDIAN_FIELD:
                return Delineation.Type.INT_LITTLE_ENDIAN_FIELD;
            case NULL_VAL:
            default:
                throw new IllegalArgumentException();
        }
    }

    public static DelineationType toWire(final Delineation.Type domain)
    {
        switch (domain)
        {
            case FIXED_LENGTH:
                return DelineationType.FIXED_LENGTH;
            case ASCII_PATTERN:
                return DelineationType.ASCII_PATTERN;
            case SHORT_BIG_ENDIAN_FIELD:
                return DelineationType.SHORT_BIG_ENDIAN_FIELD;
            case SHORT_LITTLE_ENDIAN_FIELD:
                return DelineationType.SHORT_LITTLE_ENDIAN_FIELD;
            case INT_BIG_ENDIAN_FIELD:
                return DelineationType.INT_BIG_ENDIAN_FIELD;
            case INT_LITTLE_ENDIAN_FIELD:
                return DelineationType.INT_LITTLE_ENDIAN_FIELD;
            default:
                throw new IllegalArgumentException();
        }
    }
}

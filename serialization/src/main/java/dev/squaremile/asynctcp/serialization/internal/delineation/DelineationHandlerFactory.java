package dev.squaremile.asynctcp.serialization.internal.delineation;

import dev.squaremile.asynctcp.transport.api.values.Delineation;

import static dev.squaremile.asynctcp.transport.api.values.Delineation.Type.ASCII_PATTERN;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.Type.FIXED_LENGTH;

class DelineationHandlerFactory
{
    public static final String FIX_MESSAGE_PATTERN = "8=[^\\u0001]+\\u00019=([0-9]+)\\u0001";

    DelineationHandler create(final Delineation delineation, final DelineationHandler delineatedDataHandler)
    {
        if (!isSupported(delineation))
        {
            throw new IllegalArgumentException(delineation + " is not supported yet");
        }
        switch (delineation.type())
        {
            case FIXED_LENGTH:
                return delineation.knownLength() == 0 ? delineatedDataHandler : new FixedLengthDelineation(delineatedDataHandler, delineation.knownLength());
            case ASCII_PATTERN:
                if (FIX_MESSAGE_PATTERN.equals(delineation.pattern()) && delineation.knownLength() == 7)
                {
                    return new FixMessageDelineation(delineatedDataHandler);
                }
            default:
                throw new IllegalArgumentException(delineation.toString());
        }
    }

    boolean isSupported(final Delineation delineation)
    {
        return (FIXED_LENGTH.equals(delineation.type()) && delineation.knownLength() >= 0 && "".equals(delineation.pattern())) ||
               (ASCII_PATTERN.equals(delineation.type()) && delineation.knownLength() == 7 && FIX_MESSAGE_PATTERN.equals(delineation.pattern()));
    }
}

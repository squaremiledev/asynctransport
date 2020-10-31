package dev.squaremile.asynctcp.serialization.internal.delineation;

import dev.squaremile.asynctcp.transport.api.values.Delineation;

import static dev.squaremile.asynctcp.transport.api.values.Delineation.Type.ASCII_PATTERN;

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
            case SHORT_BIG_ENDIAN_FIELD:
            case SHORT_LITTLE_ENDIAN_FIELD:
            case INT_BIG_ENDIAN_FIELD:
            case INT_LITTLE_ENDIAN_FIELD:
                return new LengthBasedDelineation(delineation.type(), delineation.padding(), delineation.extraLength(), delineatedDataHandler);
            case FIXED_LENGTH:
                return delineation.extraLength() == 0 ? delineatedDataHandler : new FixedLengthDelineation(delineatedDataHandler, delineation.extraLength());
            case ASCII_PATTERN:
                if (FIX_MESSAGE_PATTERN.equals(delineation.pattern()) && delineation.extraLength() == 7)
                {
                    return new FixMessageDelineation(delineatedDataHandler);
                }
            default:
                throw new IllegalArgumentException(delineation.toString());
        }
    }

    boolean isSupported(final Delineation delineation)
    {
        if (delineation.extraLength() < 0 || delineation.padding() < 0)
        {
            return false;
        }
        if (!ASCII_PATTERN.equals(delineation.type()) && "".equals(delineation.pattern()))
        {
            return true;
        }
        return delineation.extraLength() == 7 && FIX_MESSAGE_PATTERN.equals(delineation.pattern());
    }
}

package dev.squaremile.asynctcp.serialization.internal.delineation;

import dev.squaremile.asynctcp.transport.api.values.Delineation;

class DelineationHandlerFactory
{
    DelineationHandler create(final Delineation delineation, final DelineationHandler delineatedDataHandler)
    {
        if (!isSupported(delineation))
        {
            throw new IllegalArgumentException(delineation + " is not supported yet");
        }
        switch (delineation.knownLength())
        {
            case 0:
                return delineatedDataHandler;
            case 1:
                return new SingleByteDelineation(delineatedDataHandler);
            case 4:
                return new IntegersDelineation(delineatedDataHandler);
            case 8:
                return new LongsDelineation(delineatedDataHandler);
            default:
                return new FixedLengthDelineation(delineatedDataHandler, delineation.knownLength());
        }

    }

    boolean isSupported(final Delineation delineation)
    {
        return delineation instanceof Delineation && delineation.knownLength() >= 0;
    }
}

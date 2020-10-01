package dev.squaremile.asynctcp.serialization.internal.delineation;

import dev.squaremile.asynctcp.transport.api.values.PredefinedTransportDelineation;

class DelineationImplementations
{
    SingleByteDelineation create(final String delineation, final DelineationHandler delineatedDataHandler)
    {
        if (!PredefinedTransportDelineation.SINGLE_BYTE.name().equals(delineation))
        {
            throw new IllegalArgumentException(delineation + " is not supported yet");
        }
        return new SingleByteDelineation(delineatedDataHandler);
    }
}

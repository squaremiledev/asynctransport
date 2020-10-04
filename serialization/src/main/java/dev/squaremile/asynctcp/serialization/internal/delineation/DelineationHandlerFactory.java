package dev.squaremile.asynctcp.serialization.internal.delineation;

import dev.squaremile.asynctcp.serialization.api.delineation.FixedLengthDelineationType;
import dev.squaremile.asynctcp.transport.api.values.DelineationType;

class DelineationHandlerFactory
{
    DelineationHandler create(final DelineationType delineation, final DelineationHandler delineatedDataHandler)
    {
        if (!isSupported(delineation))
        {
            throw new IllegalArgumentException(delineation + " is not supported yet");
        }
        switch (delineation.fixedLength())
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
                return new FixedLengthDelineation(delineatedDataHandler, delineation.fixedLength());
        }

    }

    boolean isSupported(final DelineationType delineation)
    {
        return delineation instanceof FixedLengthDelineationType && delineation.fixedLength() >= 0;
    }
}

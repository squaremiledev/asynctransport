package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.util.HashSet;
import java.util.Set;


import dev.squaremile.asynctcp.transport.api.values.PredefinedTransportDelineation;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

class DelineationImplementations
{
    private static final Set<String> SUPPORTED_DELINEATION = unmodifiableSet(new HashSet<>(asList(
            PredefinedTransportDelineation.SINGLE_BYTE.name(),
            PredefinedTransportDelineation.INTEGERS.name(),
            PredefinedTransportDelineation.LONGS.name()
    )));

    DelineationHandler create(final String delineation, final DelineationHandler delineatedDataHandler)
    {
        if (!isSupported(delineation))
        {
            throw new IllegalArgumentException(delineation + " is not supported yet");
        }
        switch (delineation)
        {
            case "SINGLE_BYTE":
                return new SingleByteDelineation(delineatedDataHandler);
            case "INTEGERS":
                return new IntegersDelineation(delineatedDataHandler);
            case "LONGS":
                return new LongsDelineation(delineatedDataHandler);
            default:
                throw new IllegalArgumentException(delineation + " is not supported yet");
        }

    }

    boolean isSupported(final String delineation)
    {
        return SUPPORTED_DELINEATION.contains(delineation);
    }
}

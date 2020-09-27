package dev.squaremile.asynctcp.api;

import dev.squaremile.asynctcp.internal.NonProdGradeTransportFactory;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;

public class AsyncTcp
{
    private final TransportFactory nonProdGradeTransportFactory = new NonProdGradeTransportFactory();
    private final TransportApplicationFactory nonProdGradeTransportApplicationFactory = new dev.squaremile.asynctcp.transport.setup.TransportAppFactory()::create;

    private static void checkType(final FactoryType type)
    {
        if (type != NON_PROD_GRADE)
        {
            throw new IllegalArgumentException();
        }
    }

    public TransportFactory transportFactory(final FactoryType type)
    {
        checkType(type);
        return nonProdGradeTransportFactory;
    }

    public TransportApplicationFactory transportAppFactory(final FactoryType type)
    {
        checkType(type);
        return nonProdGradeTransportApplicationFactory;
    }
}

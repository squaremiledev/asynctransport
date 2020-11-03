package dev.squaremile.asynctcp.api;

import dev.squaremile.asynctcp.internal.NonProdGradeTransportAppFactory;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;

public class AsyncTcp
{
    private final TransportApplicationFactory nonProdGradeTransportApplicationFactory = new NonProdGradeTransportAppFactory();

    private static void checkType(final FactoryType type)
    {
        if (type != NON_PROD_GRADE)
        {
            throw new IllegalArgumentException();
        }
    }

    public TransportApplicationFactory transportAppFactory(final FactoryType type)
    {
        checkType(type);
        return nonProdGradeTransportApplicationFactory;
    }
}

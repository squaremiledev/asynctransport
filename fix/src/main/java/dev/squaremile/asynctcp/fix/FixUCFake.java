package dev.squaremile.asynctcp.fix;

import dev.squaremile.asynctcp.api.wiring.ConnectionApplicationFactory;

public class FixUCFake implements FixUC
{
    private final FixUC fixUC;
    private final ConnectionApplicationFactory applicationFactory;

    public FixUCFake(final FixUC fixUC, final ConnectionApplicationFactory applicationFactory)
    {
        this.fixUC = fixUC;
        this.applicationFactory = applicationFactory;
    }


    @Override
    public String fixVersion()
    {
        return fixUC.fixVersion();
    }

    @Override
    public String username()
    {
        return fixUC.username();
    }

    public ConnectionApplicationFactory applicationFactory()
    {
        return applicationFactory;
    }
}

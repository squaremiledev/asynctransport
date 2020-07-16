package com.michaelszymczak.sample.sockets.support;

public class TransportUnderTest extends TestableTransport<TransportEventsSpy>
{

    private final ConnectionEventsSpy connectionEventsSpy;

    public TransportUnderTest()
    {
        this(new TransportEventsSpy());
    }

    public TransportUnderTest(final TransportEventsSpy events)
    {
        super(events);
        connectionEventsSpy = new ConnectionEventsSpy(events);
    }

    public ConnectionEventsSpy connectionEvents()
    {
        return connectionEventsSpy;
    }
}

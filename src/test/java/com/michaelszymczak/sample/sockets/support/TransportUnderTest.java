package com.michaelszymczak.sample.sockets.support;

public class TransportUnderTest extends TestableTransport<TransportEventsSpy>
{
    public TransportUnderTest()
    {
        this(new TransportEventsSpy());
    }

    public TransportUnderTest(final TransportEventsSpy events)
    {
        super(events);
    }
}

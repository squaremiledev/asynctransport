package dev.squaremile.asynctcp.transport.testfixtures;

public class TransportUnderTest extends TestableTransport<TransportEventsSpy>
{

    private final ConnectionEventsSpy connectionEventsSpy;
    private final StatusEventsSpy statusEventsSpy;

    public TransportUnderTest()
    {
        this(new TransportEventsSpy(), new StatusEventsSpy());
    }

    private TransportUnderTest(final TransportEventsSpy transportEventsSpy, final StatusEventsSpy statusEventsSpy)
    {
        super(transportEventsSpy, statusEventsSpy);
        this.connectionEventsSpy = new ConnectionEventsSpy(transportEventsSpy);
        this.statusEventsSpy = statusEventsSpy;
    }

    public ConnectionEventsSpy connectionEvents()
    {
        return connectionEventsSpy;
    }

    public StatusEventsSpy statusEvents()
    {
        return statusEventsSpy;
    }
}

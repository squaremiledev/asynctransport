package dev.squaremile.transport.aerontcpgateway.api;

import io.aeron.Aeron;

public class AeronConnection
{
    private final int toNetworkStreamId;
    private final int fromNetworStreamId;
    private final String aeronDirectoryName;

    public AeronConnection(final int toNetworkStreamId, final int fromNetworStreamId, final String aeronDirectoryName)
    {
        if (toNetworkStreamId == fromNetworStreamId)
        {
            throw new IllegalArgumentException("Two unique stream ids are required");
        }
        this.toNetworkStreamId = toNetworkStreamId;
        this.fromNetworStreamId = fromNetworStreamId;
        this.aeronDirectoryName = aeronDirectoryName;
    }

    public String channel()
    {
        return "aeron:ipc";
    }

    public Aeron.Context aeronContext()
    {
        return new Aeron.Context().aeronDirectoryName(aeronDirectoryName);
    }

    public int toNetworAeronStreamId()
    {
        return toNetworkStreamId;
    }

    public int fromNetworAeronStreamId()
    {
        return fromNetworStreamId;
    }
}

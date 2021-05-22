package dev.squaremile.transport.aeron;

import io.aeron.Aeron;

public class AeronConnection
{

    private final int toNetworAeronStreamId;
    private final int fromNetworAeronStreamId;
    private final String aeronDirectoryName;

    public AeronConnection(final int toNetworAeronStreamId, final int fromNetworAeronStreamId, final String aeronDirectoryName)
    {

        this.toNetworAeronStreamId = toNetworAeronStreamId;
        this.fromNetworAeronStreamId = fromNetworAeronStreamId;
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
        return toNetworAeronStreamId;
    }

    public int fromNetworAeronStreamId()
    {
        return fromNetworAeronStreamId;
    }
}

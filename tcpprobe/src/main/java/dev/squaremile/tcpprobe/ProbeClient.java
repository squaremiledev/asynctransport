package dev.squaremile.tcpprobe;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class ProbeClient
{
    private final Metadata metadata;

    public ProbeClient(final int optionsOffset, final int sendTimeOffset, final int correlationIdOffset)
    {
        metadata = new Metadata(optionsOffset, sendTimeOffset, correlationIdOffset);
    }

    public boolean onMessage(final DirectBuffer inboundBuffer, final int inboundOffset, final MutableDirectBuffer outboundBuffer, final int outboundOffset, final int outboundAvailableLength)
    {
        metadata.wrap(inboundBuffer, inboundOffset);
        if (!metadata.options().respond())
        {
            return false;
        }
        long sendTimeNs = metadata.originalTimestampNs();
        long correlationId = metadata.correlationId();
        metadata.wrap(outboundBuffer, outboundOffset, outboundAvailableLength)
                .clear()
                .originalTimestampNs(sendTimeNs)
                .correlationId(correlationId);
        return true;
    }
}

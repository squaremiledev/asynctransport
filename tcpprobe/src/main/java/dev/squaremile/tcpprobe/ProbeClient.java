package dev.squaremile.tcpprobe;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public class ProbeClient
{
    private final Metadata metadata = new Metadata();

    public int onMessage(final DirectBuffer inboundBuffer, final int inboundOffset, final MutableDirectBuffer outboundBuffer, final int outboundOffset)
    {
        metadata.wrap(inboundBuffer, inboundOffset);
        if (!metadata.options().respond())
        {
            return 0;
        }
        long sendTimeNs = metadata.originalTimestampNs();
        long correlationId = metadata.correlationId();
        metadata.wrap(outboundBuffer, outboundOffset)
                .clear()
                .originalTimestampNs(sendTimeNs)
                .correlationId(correlationId);
        return metadata.length();
    }
}

package dev.squaremile.aeroncluster.api;

import org.agrona.DirectBuffer;

public interface ClusterClientPublisher
{
    void publish(DirectBuffer buffer, int offset, int length);
}

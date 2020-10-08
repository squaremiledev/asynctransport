package dev.squaremile.asynctcp.transport.api.app;

public interface TransportOnDuty extends AutoCloseable, Transport, OnDuty
{
    @Override
    void close();
}

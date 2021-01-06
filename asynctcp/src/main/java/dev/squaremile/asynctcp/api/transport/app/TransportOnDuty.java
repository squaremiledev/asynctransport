package dev.squaremile.asynctcp.api.transport.app;

public interface TransportOnDuty extends AutoCloseable, OnDuty
{
    @Override
    void close();
}

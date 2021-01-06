package dev.squaremile.asynctcp.internal.transport.domain.connection;

public enum ConnectionState
{
    NO_OUTSTANDING_DATA, DATA_TO_SEND_BUFFERED, CLOSED
}

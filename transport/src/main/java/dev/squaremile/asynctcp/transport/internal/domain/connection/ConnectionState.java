package dev.squaremile.asynctcp.transport.internal.domain.connection;

public enum ConnectionState
{
    NO_OUTSTANDING_DATA, DATA_TO_SEND_BUFFERED, CLOSED
}

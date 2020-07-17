package com.michaelszymczak.sample.sockets.nio;

import com.michaelszymczak.sample.sockets.api.events.NumberOfConnectionsChanged;
import com.michaelszymczak.sample.sockets.api.events.StatusEventListener;
import com.michaelszymczak.sample.sockets.connection.ConnectionRepository;

class StatusRepositoryUpdates implements ConnectionRepository.RepositoryUpdates
{
    private StatusEventListener statusEventListener;

    public StatusRepositoryUpdates(final StatusEventListener statusEventListener)
    {
        this.statusEventListener = statusEventListener;
    }

    @Override
    public void onNumberOfConnectionsChanged(final int newNumberOfConnections)
    {
        statusEventListener.onEvent(new NumberOfConnectionsChanged(newNumberOfConnections));
    }
}

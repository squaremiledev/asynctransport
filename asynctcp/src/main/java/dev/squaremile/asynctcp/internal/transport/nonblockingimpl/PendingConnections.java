package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

import org.agrona.concurrent.EpochClock;


import dev.squaremile.asynctcp.api.transport.app.EventListener;
import dev.squaremile.asynctcp.api.transport.events.TransportCommandFailed;

public class PendingConnections
{
    private final List<ConnectedNotification> pending = new ArrayList<>(8);
    private final EpochClock clock;
    private final EventListener eventListener;

    public PendingConnections(final EpochClock clock, final EventListener eventListener)
    {
        this.clock = clock;
        this.eventListener = eventListener;
    }

    ConnectedNotification pendingConnection(final SelectionKey key)
    {
        for (final ConnectedNotification connectedNotification : pending)
        {
            if (connectedNotification.selectionKey == key)
            {
                return connectedNotification;
            }
        }

        throw new IllegalStateException(key + " is not associated with a pending connection");
    }

    void removePendingConnection(final SelectionKey key)
    {
        int found = -1;
        for (int i = 0; i < pending.size(); i++)
        {
            if (pending.get(i).selectionKey == key)
            {
                found = i;
                break;
            }
        }
        if (found >= 0)
        {
            pending.remove(found);
        }
    }

    public void add(final ConnectedNotification connectedNotification)
    {
        pending.add(connectedNotification);
    }

    public void work()
    {
        int foundAfterDeadline = -1;
        for (int i = 0; i < pending.size(); i++)
        {
            long currentTimeMs = clock.time();
            ConnectedNotification connectedNotification = pending.get(i);
            if (currentTimeMs > connectedNotification.deadlineMs)
            {
                foundAfterDeadline = i;
                break;
            }
        }
        if (foundAfterDeadline >= 0)
        {
            ConnectedNotification connectedNotification = pending.get(foundAfterDeadline);
            connectedNotification.selectionKey.cancel();
            pending.remove(foundAfterDeadline);
            eventListener.onEvent(new TransportCommandFailed(connectedNotification.command, "Timed out"));
        }
    }
}

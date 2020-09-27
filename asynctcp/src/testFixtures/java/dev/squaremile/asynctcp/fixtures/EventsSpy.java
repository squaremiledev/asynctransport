package dev.squaremile.asynctcp.fixtures;

import java.util.ArrayList;
import java.util.List;


import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventListener;

public class EventsSpy implements EventListener
{
    private final List<Event> received = new ArrayList<>();

    @Override
    public void onEvent(final Event event)
    {
        received.add(event.copy());
    }

    public List<Event> received()
    {
        return received;
    }
}

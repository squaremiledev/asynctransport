package com.michaelszymczak.sample.sockets.connection;

import com.michaelszymczak.sample.sockets.api.events.ConnectionEvent;

public interface ConnectionEventsListener
{
    void onEvent(ConnectionEvent event);
}

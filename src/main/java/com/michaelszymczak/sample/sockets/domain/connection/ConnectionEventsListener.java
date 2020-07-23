package com.michaelszymczak.sample.sockets.domain.connection;

import com.michaelszymczak.sample.sockets.domain.api.events.ConnectionEvent;

public interface ConnectionEventsListener
{
    void onEvent(ConnectionEvent event);
}

package com.michaelszymczak.sample.sockets.domain.api.events;

import com.michaelszymczak.sample.sockets.domain.api.TransportId;

public interface TransportEvent extends TransportId, Event
{
    TransportEvent copy();
}

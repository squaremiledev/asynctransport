package com.michaelszymczak.sample.sockets.api.events;

import com.michaelszymczak.sample.sockets.api.TransportId;

public interface TransportEvent extends TransportId
{
    TransportEvent copy();
}

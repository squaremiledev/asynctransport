package com.michaelszymczak.sample.sockets.api.events;

import com.michaelszymczak.sample.sockets.api.CommandId;

public interface TransportCorrelatedEvent extends TransportEvent, CommandId
{
}

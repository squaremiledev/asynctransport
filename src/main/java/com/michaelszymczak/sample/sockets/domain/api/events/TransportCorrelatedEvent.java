package com.michaelszymczak.sample.sockets.domain.api.events;

import com.michaelszymczak.sample.sockets.domain.api.CommandId;

public interface TransportCorrelatedEvent extends TransportEvent, CommandId
{
}

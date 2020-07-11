package com.michaelszymczak.sample.sockets.api.commands;

public interface ConnectionCommand extends TransportCommand
{
    long connectionId();
}

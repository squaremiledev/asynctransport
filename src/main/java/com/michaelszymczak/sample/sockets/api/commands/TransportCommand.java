package com.michaelszymczak.sample.sockets.api.commands;

public interface TransportCommand
{
    long CONVENTIONAL_IGNORED_COMMAND_ID = -1;

    int port();

    long commandId();
}

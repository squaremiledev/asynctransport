package com.michaelszymczak.sample.sockets.api.commands;

public interface Command
{
    long NO_COMMAND_ID = -1;

    long commandId();
}

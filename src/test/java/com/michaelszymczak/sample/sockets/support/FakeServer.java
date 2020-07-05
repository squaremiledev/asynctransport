package com.michaelszymczak.sample.sockets.support;

public interface FakeServer
{
    int port();

    void waitUntilReady();

    void startServer();
}

package dev.squaremile.asynctcp.transport.api.app;

public interface ApplicationEmittingEventsFactory
{
    Application create(Transport transport, EventListener eventListener);
}

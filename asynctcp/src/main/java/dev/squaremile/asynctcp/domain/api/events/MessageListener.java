package dev.squaremile.asynctcp.domain.api.events;

public interface MessageListener
{
    void onMessage(final MessageReceived messageReceived);
}
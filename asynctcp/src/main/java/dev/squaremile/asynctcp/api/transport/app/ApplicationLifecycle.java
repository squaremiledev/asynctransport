package dev.squaremile.asynctcp.api.transport.app;

public interface ApplicationLifecycle
{
    void onStart();

    void onStop();
}

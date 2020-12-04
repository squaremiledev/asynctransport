package dev.squaremile.asynctcp.transport.api.app;

public interface ApplicationLifecycle
{
    void onStart();

    void onStop();
}

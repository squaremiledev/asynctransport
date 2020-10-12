package dev.squaremile.asynctcp.transport.api.app;

public interface ApplicationOnDuty extends OnDuty
{
    void onStart();

    void onStop();
}

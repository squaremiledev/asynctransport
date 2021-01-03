package dev.squaremile.transport.casestudy.marketmaking.domain;

@FunctionalInterface
public interface HeartBeatListener
{
    HeartBeatListener IGNORE = heartBeat ->
    {
    };

    void onHeartBeat(HeartBeat heartBeat);
}

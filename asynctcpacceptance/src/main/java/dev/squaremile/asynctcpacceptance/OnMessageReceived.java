package dev.squaremile.asynctcpacceptance;

public interface OnMessageReceived
{
    void onMessageReceived(long messagesSentCount, long messagesReceivedCount, long messageSentTimeNs, long messageReceivedTimeNs);
}

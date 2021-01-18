package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

class SocketProtection
{
    private static final int DEFAULT_COOL_DOWN_NS = 6_000;
    private static final int SOCKET_PROTECTING_COOL_DOWN_NS = DEFAULT_COOL_DOWN_NS * 4;
    private static final int EMERGENCY_COOL_DOWN_NS = DEFAULT_COOL_DOWN_NS * 16;

    public static long socketCoolDownNs(final long sendDataRequestCountResetNs, final long now, final long sendDataRequestCount)
    {
        if (sendDataRequestCount == 0)
        {
            return DEFAULT_COOL_DOWN_NS;
        }

        final long timeSinceCountResetNs = now - sendDataRequestCountResetNs;
        final long avgNsBetweenNewDataSendRequests = (int)timeSinceCountResetNs / sendDataRequestCount;

        if (avgNsBetweenNewDataSendRequests > 2_000)
        {
            return DEFAULT_COOL_DOWN_NS;
        }
        if (avgNsBetweenNewDataSendRequests > 1_000)
        {
            return SOCKET_PROTECTING_COOL_DOWN_NS;
        }
        return EMERGENCY_COOL_DOWN_NS;
    }
}

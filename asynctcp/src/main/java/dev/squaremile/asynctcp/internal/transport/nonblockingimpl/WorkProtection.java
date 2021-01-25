package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

import java.util.concurrent.TimeUnit;

class WorkProtection
{
    private static final long RESET_INTERVAL_NS = TimeUnit.MILLISECONDS.toNanos(1);
    private static final String DEBUG_ROLE = null;
//    private static final String DEBUG_ROLE = "source";

    private final String role;
    public long handleRequestsCount = 0;
    public long workRequestsCount = 0;
    public long workProtectionResetNs = 0;
    private long timesReset = 0;
    private long nextWorkAllowedNs = 0;
    private int calculatedCoolDownNs = 0;

    public WorkProtection(final String role)
    {
        this.role = role;
    }

    public boolean nextWorkAllowedNs(final long nowNs)
    {
        if (nowNs < nextWorkAllowedNs)
        {
            return false;
        }

        calculatedCoolDownNs = calculateCoolDown(nowNs);
        periodicReset(nowNs);
        nextWorkAllowedNs = nowNs + calculatedCoolDownNs;
        return true;
    }

    public void onWork()
    {
        workRequestsCount++;
    }

    public void onHandle()
    {
        handleRequestsCount++;
    }

    @Override
    public String toString()
    {
        return "WorkProtection{" +
               "role='" + role + '\'' +
               ", handleRequestsCount=" + handleRequestsCount +
               ", workRequestsCount=" + workRequestsCount +
               ", workProtectionResetNs=" + workProtectionResetNs +
               ", timesReset=" + timesReset +
               ", nextWorkAllowedNs=" + nextWorkAllowedNs +
               ", calculatedCoolDownNs=" + calculatedCoolDownNs +
               '}';
    }

    private void periodicReset(final long nowNs)
    {
        if (workProtectionResetNs + RESET_INTERVAL_NS < nowNs)
        {
            debug();
            workRequestsCount = 0;
            handleRequestsCount = 0;
            workProtectionResetNs = nowNs;
            timesReset++;
        }
    }

    private void debug()
    {
        if (DEBUG_ROLE != null && DEBUG_ROLE.equals(role) && timesReset % 5000 == 0)
        {
            System.out.println(toString());
        }
    }

    private int calculateCoolDown(final long nowNs)
    {
        final int result;
        if (handleRequestsCount == 0)
        {
            result = 0;
        }
        else if (handleRequestsCount >= workRequestsCount)
        {
            result = 10_000;
        }
        else
        {
            final long timeSinceCountResetNs = nowNs - workProtectionResetNs;
            final long avgNsBetweenNewDataSendRequests = (int)timeSinceCountResetNs / handleRequestsCount;

            if (avgNsBetweenNewDataSendRequests > 10_000)
            {
                result = 0;
            }
            else if (avgNsBetweenNewDataSendRequests > 5_000)
            {
                result = 500;
            }
            else if (avgNsBetweenNewDataSendRequests > 2_000)
            {
                result = 1000;
            }
            else if (avgNsBetweenNewDataSendRequests > 1_000)
            {
                result = 2000;
            }
            else
            {
                result = 8000;
            }
        }

        return result;
    }


}

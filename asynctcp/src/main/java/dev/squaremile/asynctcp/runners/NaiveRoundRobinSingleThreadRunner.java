package dev.squaremile.asynctcp.runners;

import org.agrona.LangUtil;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.status.AtomicCounter;

import static org.agrona.concurrent.AgentRunner.startOnThread;


import dev.squaremile.asynctcp.application.TransportApplication;

import static java.lang.System.arraycopy;

public class NaiveRoundRobinSingleThreadRunner
{
    public void run(final TransportApplication... transportApplication)
    {
        startOnThread(new AgentRunner(
                new SleepingIdleStrategy(),
                LangUtil::rethrowUnchecked,
                new AtomicCounter(new UnsafeBuffer(new byte[512]), 1),
                new RoundRobinTransportAppAgent(transportApplication)
        ));
    }

    private static class RoundRobinTransportAppAgent implements Agent
    {
        private final TransportApplication[] transportApplications;

        RoundRobinTransportAppAgent(final TransportApplication... transportApplications)
        {
            this.transportApplications = new TransportApplication[transportApplications.length];
            arraycopy(transportApplications, 0, this.transportApplications, 0, transportApplications.length);
        }

        @Override
        public void onStart()
        {
            for (final TransportApplication application : transportApplications)
            {
                application.onStart();
            }
        }

        @Override
        public int doWork()
        {
            for (final TransportApplication application : transportApplications)
            {
                application.work();
            }
            return transportApplications.length;
        }

        @Override
        public void onClose()
        {
            for (final TransportApplication application : transportApplications)
            {
                application.onStop();
            }
        }

        @Override
        public String roleName()
        {
            return "appRunner";
        }
    }
}

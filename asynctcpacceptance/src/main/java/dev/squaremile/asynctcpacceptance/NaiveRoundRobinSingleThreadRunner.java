package dev.squaremile.asynctcpacceptance;

import java.util.ArrayList;
import java.util.List;

import org.agrona.LangUtil;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.status.AtomicCounter;

import static org.agrona.concurrent.AgentRunner.startOnThread;


import dev.squaremile.asynctcp.transport.api.app.Application;

public class NaiveRoundRobinSingleThreadRunner
{
    public void run(final List<Application> transportApplication)
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
        private final List<Application> transportApplications;

        RoundRobinTransportAppAgent(final List<Application> transportApplications)
        {
            this.transportApplications = new ArrayList<>(transportApplications);
        }

        @Override
        public void onStart()
        {
            for (final Application application : transportApplications)
            {
                application.onStart();
            }
        }

        @Override
        public int doWork()
        {
            for (final Application application : transportApplications)
            {
                application.work();
            }
            return transportApplications.size();
        }

        @Override
        public void onClose()
        {
            for (final Application application : transportApplications)
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

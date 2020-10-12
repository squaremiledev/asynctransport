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


import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;

public class NaiveRoundRobinSingleThreadRunner
{
    public void run(final List<ApplicationOnDuty> transportApplication)
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
        private final List<ApplicationOnDuty> transportApplications;

        RoundRobinTransportAppAgent(final List<ApplicationOnDuty> transportApplications)
        {
            this.transportApplications = new ArrayList<>(transportApplications);
        }

        @Override
        public void onStart()
        {
            for (final ApplicationOnDuty application : transportApplications)
            {
                application.onStart();
            }
        }

        @Override
        public int doWork()
        {
            for (final ApplicationOnDuty application : transportApplications)
            {
                application.work();
            }
            return transportApplications.size();
        }

        @Override
        public void onClose()
        {
            for (final ApplicationOnDuty application : transportApplications)
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

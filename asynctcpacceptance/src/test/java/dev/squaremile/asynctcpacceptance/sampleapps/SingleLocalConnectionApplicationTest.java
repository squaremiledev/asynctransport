package dev.squaremile.asynctcpacceptance.sampleapps;

import org.junit.jupiter.api.Test;


import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.Application;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.transport.api.values.Delineation.fixedLengthDelineation;

class SingleLocalConnectionApplicationTest
{
    private final ApplicationLifecycle lifecycleListener = new ApplicationLifecycle();

    @Test
    void measureRoundTripTime()
    {
        Application app = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
                "roundTripTime", transport -> new SingleLocalConnectionApplication(transport, fixedLengthDelineation(16), lifecycleListener, System.out::println));
        app.onStart();

        while (!lifecycleListener.isUp())
        {
            app.work();
        }

        app.onStop();

        while (lifecycleListener.isUp())
        {
            app.work();
        }
    }

    private static class ApplicationLifecycle implements SingleLocalConnectionApplication.LifecycleListener
    {
        private boolean isUp = false;

        @Override
        public void onUp()
        {
            isUp = true;
        }

        @Override
        public void onDown()
        {
            isUp = false;
        }

        public boolean isUp()
        {
            return isUp;
        }
    }
}
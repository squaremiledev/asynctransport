package dev.squaremile.asynctcpacceptance.sampleapps;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import dev.squaremile.asynctcp.api.app.Application;
import dev.squaremile.asynctcp.api.app.Event;
import dev.squaremile.asynctcp.api.events.StartedListening;

import static java.util.Arrays.asList;

class MessageEncodingApplicationTest
{

    private ApplicationSpy applicationSpy = new ApplicationSpy();

    @Test
    void shouldDelegateToUnderlyingApplication()
    {
        MessageEncodingApplication app = new MessageEncodingApplication(applicationSpy);
        assertThat(applicationSpy.invoked()).isEmpty();

        // When
        app.onStart();
        app.onStop();
        app.work();
        app.onEvent(new StartedListening(8888, 5));

        // Then
        assertThat(applicationSpy.invoked()).isEqualTo(asList(
                "onStart", "onStop", "work", "onEvent: " + new StartedListening(8888, 5).toString()
        ));
    }

    private static class ApplicationSpy implements Application
    {
        private final List<String> invoked = new ArrayList<>();

        List<String> invoked()
        {
            return invoked;
        }

        @Override
        public void onStart()
        {
            invoked.add("onStart");
        }

        @Override
        public void onStop()
        {
            invoked.add("onStop");
        }

        @Override
        public void work()
        {
            invoked.add("work");
        }

        @Override
        public void onEvent(final Event event)
        {
            invoked.add("onEvent: " + event.toString());
        }
    }
}
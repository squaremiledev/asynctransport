package dev.squaremile.asynctcpacceptance.sampleapps;


import java.util.function.Consumer;


import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.Transport;
import dev.squaremile.asynctcp.transport.api.commands.CloseConnection;
import dev.squaremile.asynctcp.transport.api.commands.Connect;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.StopListening;
import dev.squaremile.asynctcp.transport.api.events.Connected;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;
import dev.squaremile.asynctcp.transport.api.events.ConnectionClosed;
import dev.squaremile.asynctcp.transport.api.events.ConnectionResetByPeer;
import dev.squaremile.asynctcp.transport.api.events.StartedListening;
import dev.squaremile.asynctcp.transport.api.events.StoppedListening;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

import static dev.squaremile.asynctcp.transport.testfixtures.FreePort.freePort;

public class SingleLocalConnectionApplication implements Application
{
    final LifecycleListener lifecycleListener;
    private final Transport t;
    private final Delineation delineation;
    private State state = State.DOWN;
    private int listeningPort;
    private ConnectionIdValue acceptorConnectionId;
    private ConnectionIdValue initiatorConnectionId;
    private final Consumer<String> log;

    public SingleLocalConnectionApplication(final Transport transport, final Delineation delineation, final LifecycleListener lifecycleListener, final Consumer<String> log)
    {
        this.t = transport;
        this.lifecycleListener = lifecycleListener;
        this.delineation = delineation;
        this.log = log;
    }

    @Override
    public void onStart()
    {
        log.accept("enter onStart() " + state);
        state(State.STARTING_UP);
        t.handle(t.command(Listen.class).set(1, freePort(), delineation));
        log.accept("exit  onStart() " + state);
    }

    @Override
    public void onStop()
    {
        log.accept("enter onStop() " + state);
        state(State.TEARING_DOWN);

        if (initiatorConnectionId != null)
        {
            t.handle(t.command(initiatorConnectionId, CloseConnection.class).set(4));
        }
        else if (acceptorConnectionId != null)
        {
            t.handle(t.command(acceptorConnectionId, CloseConnection.class).set(5));
        }
        log.accept("exit  onStop() " + state);
    }

    @Override
    public void work()
    {

    }

    private void state(final State newState)
    {
        final State previousState = this.state;
        this.state = newState;
        if (state != previousState)
        {
            log.accept("transition " + previousState + " -> " + newState);
            if (state == State.UP)
            {
                lifecycleListener.onUp();
            }
            if (state == State.DOWN)
            {
                lifecycleListener.onDown();
            }
        }
    }

    @Override
    public void onEvent(final Event event)
    {
        if (!event.occursInSteadyState())
        {
            log.accept("enter onEvent() " + state + " " + event);
        }
        if (event instanceof StartedListening)
        {
            StartedListening startedListening = (StartedListening)event;
            listeningPort = startedListening.port();
            t.handle(t.command(Connect.class).set("localhost", startedListening.port(), 2, 1000, startedListening.delineation()));
        }
        if (event instanceof ConnectionAccepted)
        {
            ConnectionAccepted connectionAccepted = (ConnectionAccepted)event;
            acceptorConnectionId = new ConnectionIdValue(connectionAccepted);
            if (initiatorConnectionId != null)
            {
                state(State.UP);
            }
            if (listeningPort > 0)
            {
                t.handle(t.command(StopListening.class).set(3, listeningPort));
            }
        }
        if (event instanceof Connected)
        {
            Connected connected = (Connected)event;
            initiatorConnectionId = new ConnectionIdValue(connected);
            if (acceptorConnectionId != null)
            {
                state(State.UP);
            }
        }
        if (event instanceof ConnectionResetByPeer || event instanceof ConnectionClosed)
        {
            if (initiatorConnectionId != null && initiatorConnectionId.connectionId() == ((ConnectionEvent)event).connectionId())
            {
                initiatorConnectionId = null;
            }
            if (acceptorConnectionId != null && acceptorConnectionId.connectionId() == ((ConnectionEvent)event).connectionId())
            {
                acceptorConnectionId = null;
            }
            if (acceptorConnectionId == null && initiatorConnectionId == null)
            {
                state(State.DOWN);
            }
        }
        if (event instanceof StoppedListening)
        {
            StoppedListening stoppedListening = (StoppedListening)event;
            if (listeningPort == stoppedListening.port())
            {
                listeningPort = 0;
            }
        }
        if (!event.occursInSteadyState())
        {
            log.accept("exit  onEvent() " + state);
        }
    }

    enum State
    {
        DOWN,
        STARTING_UP,
        UP,
        TEARING_DOWN
    }

    interface LifecycleListener
    {
        void onUp();

        void onDown();
    }


}

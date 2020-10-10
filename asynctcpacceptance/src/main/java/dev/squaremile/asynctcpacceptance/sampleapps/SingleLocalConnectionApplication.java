package dev.squaremile.asynctcpacceptance.sampleapps;


import java.util.function.Consumer;


import dev.squaremile.asynctcp.transport.api.app.Application;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionCommand;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.app.ConnectionUserCommand;
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
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;
import dev.squaremile.asynctcp.transport.api.values.Delineation;

public class SingleLocalConnectionApplication implements Application
{
    final LifecycleListener lifecycleListener;
    private final Transport t;
    private final Delineation delineation;
    private final Consumer<String> log;
    private final int port;
    private final ConnectionApplicationFactory acceptingConnectionApplicationFactory;
    private final ConnectionApplicationFactory initiatingConnectionApplicationFactory;
    private State state = State.DOWN;
    private int listeningPort;
    private ConnectionIdValue acceptorConnectionId;
    private ConnectionIdValue initiatorConnectionId;
    private ConnectionApplication acceptor;
    private ConnectionApplication initiator;

    public SingleLocalConnectionApplication(
            final Transport transport,
            final Delineation delineation,
            final LifecycleListener lifecycleListener,
            final Consumer<String> log,
            final int port,
            final ConnectionApplicationFactory acceptorFactory,
            final ConnectionApplicationFactory initiatorFactory
    )
    {
        this.t = transport;
        this.lifecycleListener = lifecycleListener;
        this.delineation = delineation;
        this.log = log;
        this.port = port;
        this.acceptingConnectionApplicationFactory = acceptorFactory;
        this.initiatingConnectionApplicationFactory = initiatorFactory;
    }

    @Override
    public void onStart()
    {
        log.accept("enter onStart() " + state);
        state(State.STARTING_UP);
        t.handle(t.command(Listen.class).set(1, port, delineation));
        log.accept("exit  onStart() " + state);
    }

    @Override
    public void onStop()
    {
        log.accept("enter onStop() " + state);
        state(State.TEARING_DOWN);
        if (initiator != null)
        {
            initiator.onStop();
        }
        if (acceptor != null)
        {
            acceptor.onStop();
        }

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
        initiator.work();
        acceptor.work();
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
        else if (event instanceof ConnectionAccepted)
        {
            ConnectionAccepted connectionAccepted = (ConnectionAccepted)event;
            acceptorConnectionId = new ConnectionIdValue(connectionAccepted);
            if (initiatorConnectionId != null)
            {
                onApplicationUp();
            }
            if (listeningPort > 0)
            {
                t.handle(t.command(StopListening.class).set(3, listeningPort));
            }
        }
        else if (event instanceof Connected)
        {
            Connected connected = (Connected)event;
            initiatorConnectionId = new ConnectionIdValue(connected);
            if (acceptorConnectionId != null)
            {
                onApplicationUp();
            }
        }
        else if (event instanceof ConnectionResetByPeer || event instanceof ConnectionClosed)
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
        else if (event instanceof StoppedListening)
        {
            StoppedListening stoppedListening = (StoppedListening)event;
            if (listeningPort == stoppedListening.port())
            {
                listeningPort = 0;
            }
        }
        else if (event instanceof ConnectionEvent)
        {
            ConnectionEvent connectionEvent = (ConnectionEvent)event;
            if (initiator != null && initiatorConnectionId != null && initiatorConnectionId.connectionId() == connectionEvent.connectionId())
            {
                initiator.onEvent(connectionEvent);
            }
            if (acceptor != null && acceptorConnectionId != null && acceptorConnectionId.connectionId() == connectionEvent.connectionId())
            {
                acceptor.onEvent(connectionEvent);
            }
        }

        if (!event.occursInSteadyState())
        {
            log.accept("exit  onEvent() " + state);
        }
    }

    private void onApplicationUp()
    {
        state(State.UP);
        if (state == State.UP && initiatorConnectionId != null && acceptorConnectionId != null)
        {
            if (initiator == null)
            {
                initiator = initiatingConnectionApplicationFactory.create(new SingleConnectionTransport(t, initiatorConnectionId), initiatorConnectionId);
                initiator.onStart();
            }
            if (acceptor == null)
            {
                acceptor = acceptingConnectionApplicationFactory.create(new SingleConnectionTransport(t, acceptorConnectionId), acceptorConnectionId);
                acceptor.onStart();
            }
        }
    }

    enum State
    {
        DOWN,
        STARTING_UP,
        UP,
        TEARING_DOWN
    }

    public interface ConnectionApplicationFactory
    {
        ConnectionApplication create(ConnectionTransport transport, ConnectionId connectionId);
    }

    public interface LifecycleListener
    {
        void onUp();

        void onDown();
    }

    public static class SingleConnectionTransport implements ConnectionTransport
    {
        private final Transport transport;
        private final ConnectionId connectionId;

        public SingleConnectionTransport(final Transport transport, final ConnectionId connectionId)
        {
            this.transport = transport;
            this.connectionId = new ConnectionIdValue(connectionId);
        }

        @Override
        public <C extends ConnectionUserCommand> C command(final Class<C> commandType)
        {
            return transport.command(connectionId, commandType);
        }

        @Override
        public void handle(final ConnectionCommand command)
        {
            if (command.connectionId() != connectionId.connectionId())
            {
                throw new IllegalArgumentException("Connection id mismatch " + command.connectionId() + " vs " + connectionId.connectionId());
            }
            transport.handle(command);
        }
    }
}

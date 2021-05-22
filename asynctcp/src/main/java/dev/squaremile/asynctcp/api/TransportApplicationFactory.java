package dev.squaremile.asynctcp.api;


import dev.squaremile.asynctcp.api.serialization.SerializedCommandListener;
import dev.squaremile.asynctcp.api.serialization.SerializedEventListener;
import dev.squaremile.asynctcp.api.serialization.SerializedMessageListener;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDutyFactory;
import dev.squaremile.asynctcp.api.transport.app.TransportOnDuty;
import dev.squaremile.asynctcp.internal.serialization.messaging.SerializedCommandSupplier;
import dev.squaremile.asynctcp.internal.serialization.messaging.SerializedEventSupplier;

public interface TransportApplicationFactory
{
    /**
     * Creates a wired TCP Application that is ready to use and uses buffers to exchange messages between
     * the transport and tha application.
     *
     * <p>
     * Use this factory if you want transport commands and events to be serialized and exchanged via buffers,
     * and not directly by a method invocations. If the overhead is acceptable, this can be used a default
     * method of communication for audit purposes.
     *
     * @param role                      A simple label, no other special meaning at the moment
     * @param buffersSize               Size of the underlying transport - app buffers in bytes (e.g. 1024 * 1024 )
     * @param serializedMessageListener Is notified about all commands and events sent from and to the application
     * @param applicationFactory        a user provided application
     * @return a wired application ready to be started and used
     */
    TransportApplicationOnDuty create(
            String role,
            int buffersSize,
            final SerializedMessageListener serializedMessageListener,
            TransportApplicationOnDutyFactory applicationFactory
    );

    /**
     * Creates a wired TCP Application that is ready to use and uses an on-stack invocation to as a mean
     * to connect the transport and the application.
     *
     * <p>
     * Use this factory if you want the least overhead at the cost of no audit trail what commands and events are exchanged
     * between your application and the transport. The interaction with the transport from within you app can be the same
     * as when intermediary buffers were used, with some caveats. The caveats are the following:
     * <p>
     * - An app and transport interaction is based on a simple method invocation. If sending command A results in event B, that the app
     * handles by sending again a command A, this infinite loop manifests as a stack overflow. Avoid sending the same retry command
     * for CommandFailure, or sending Data or Message in response to a DataSent event.
     * <p>
     * - If an app is notified about an event and responds with some command, this is an interrupt call and the transport has not
     * finished processing the method that notified about the event yet. This should not cause any issues to the application, but
     * it's worth keeping in mind when debugging command processing on the library side to spare the confusion.
     *
     * @param role               A simple label, no other special meaning at the moment
     * @param applicationFactory a user provided application
     * @return a wired application ready to be started and used
     */
    TransportApplicationOnDuty createSharedStack(String role, TransportApplicationOnDutyFactory applicationFactory);

    /**
     * Creates a wired TCP Application that requires a buffer-backed transport counterparty to work.
     * <p>
     * Use this factory along with a corresponding {@link TransportApplicationFactory#createTransport(String, SerializedCommandSupplier, SerializedEventListener)}
     * method to have an application that is independent from the transport, or to run
     * the application and the transport in separate threads or processes.
     *
     * @param role                    A simple label, no other special meaning at the moment
     * @param applicationFactory      A user provided application
     * @param eventSupplier           A source of serialized events
     * @param commandListener         A listener for serialized commands that should be sent to the transport
     * @param serializedEventListener An additional listener for serialized events that come from the transport
     * @return a wired application ready to be started and used
     */
    TransportApplicationOnDuty createWithoutTransport(
            String role,
            TransportApplicationOnDutyFactory applicationFactory,
            SerializedEventSupplier eventSupplier,
            SerializedCommandListener commandListener,
            SerializedEventListener serializedEventListener
    );

    /**
     * Creates a buffer backed TCP transport.
     *
     * @param role            A simple label, no other special meaning at the moment
     * @param commandSupplier A source os serialized commands
     * @param eventListener   A listener for serialized events
     * @return a wired transport
     */
    TransportOnDuty createTransport(String role, SerializedCommandSupplier commandSupplier, SerializedEventListener eventListener);
}

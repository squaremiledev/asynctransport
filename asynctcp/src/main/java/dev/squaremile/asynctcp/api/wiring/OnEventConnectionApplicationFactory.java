package dev.squaremile.asynctcp.api.wiring;

import java.util.Optional;


import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionEvent;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;

public interface OnEventConnectionApplicationFactory
{
    /**
     * Creates an application for a single connection.
     * <p>
     * It does not have to create the application immediately. It can return null until enough data is gathered
     * to decide about the application implementation to create.
     * <p>
     * - The event that caused the first non-null factory response and all subsequent ones are passed on to the created application
     * <p>
     * - The factory needs to return a non-null response only once, as it is not queried again afterwards.
     * <p>
     * A possible use case can be an application details of which are not known until a logon message has been received.
     * In this case, the implementation of this factory reads the messages until the logon message is received and create
     * the appropriate implementation of the application accordingly.
     *
     * @param connectionTransport underlying transport
     * @param event               evens targeting a particular connection
     * @return application handling a single connection or null if not decided yet
     */
    Optional<ConnectionApplication> createOnEvent(final ConnectionTransport connectionTransport, final ConnectionEvent event);
}

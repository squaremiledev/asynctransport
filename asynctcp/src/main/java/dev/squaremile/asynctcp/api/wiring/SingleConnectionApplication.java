package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.asynctcp.transport.api.values.ConnectionIdValue;

class SingleConnectionApplication
{
    final ConnectionIdValue connectionId;
    final ConnectionApplication application;

    public SingleConnectionApplication(final ConnectionId connectionId, final ConnectionApplication application)
    {
        this.connectionId = new ConnectionIdValue(connectionId);
        this.application = application;
    }
}

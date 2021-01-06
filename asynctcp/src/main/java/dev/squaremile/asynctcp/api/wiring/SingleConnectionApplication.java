package dev.squaremile.asynctcp.api.wiring;

import dev.squaremile.asynctcp.api.transport.app.ConnectionApplication;
import dev.squaremile.asynctcp.api.transport.values.ConnectionId;
import dev.squaremile.asynctcp.api.transport.values.ConnectionIdValue;

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

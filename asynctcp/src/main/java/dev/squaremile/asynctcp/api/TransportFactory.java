package dev.squaremile.asynctcp.api;


import dev.squaremile.asynctcp.serialization.api.SerializedEventListener;
import dev.squaremile.asynctcp.serialization.internal.messaging.SerializedCommandSupplier;
import dev.squaremile.asynctcp.transport.api.app.TransportOnDuty;

public interface TransportFactory
{
    TransportOnDuty create(String role, SerializedEventListener eventListener);

    TransportOnDuty create(String role, SerializedCommandSupplier commandSupplier, SerializedEventListener eventListener);
}

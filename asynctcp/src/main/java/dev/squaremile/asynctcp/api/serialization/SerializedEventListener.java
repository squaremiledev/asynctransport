package dev.squaremile.asynctcp.api.serialization;

public interface SerializedEventListener extends SerializedMessageListener
{
    SerializedEventListener NO_OP = (sourceBuffer, sourceOffset, length) ->
    {
    };
}

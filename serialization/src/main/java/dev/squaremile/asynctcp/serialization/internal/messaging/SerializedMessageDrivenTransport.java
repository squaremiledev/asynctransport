package dev.squaremile.asynctcp.serialization.internal.messaging;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.MessageHandler;


import dev.squaremile.asynctcp.serialization.api.MessageDrivenTransport;

public class SerializedMessageDrivenTransport implements MessageDrivenTransport
{
    private final SerializedCommandSupplier commandSupplier;
    private final MessageHandler messageHandler;
    private final MessageDrivenTransport messageDrivenTransport;

    public SerializedMessageDrivenTransport(final MessageDrivenTransport messageDrivenTransport, final SerializedCommandSupplier commandSupplier)
    {
        this.messageDrivenTransport = messageDrivenTransport;
        this.commandSupplier = commandSupplier;
        this.messageHandler = (msgTypeId, buffer, index, length) -> messageDrivenTransport.onSerialized(buffer, index, length);
    }

    @Override
    public void onSerialized(final DirectBuffer sourceBuffer, final int sourceOffset, final int length)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void work()
    {
        commandSupplier.poll(messageHandler);
        messageDrivenTransport.work();
    }

    @Override
    public void close()
    {
        messageDrivenTransport.close();
    }
}

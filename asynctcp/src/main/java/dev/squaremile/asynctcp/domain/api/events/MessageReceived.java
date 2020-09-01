package dev.squaremile.asynctcp.domain.api.events;

public class MessageReceived implements ConnectionEvent
{
    private DataReceived dataReceived;

    public DataReceived dataReceived()
    {
        return dataReceived;
    }

    @Override
    public int port()
    {
        return dataReceived.port();
    }

    @Override
    public long connectionId()
    {
        return dataReceived.connectionId();
    }


    public MessageReceived set(final DataReceived dataReceived)
    {
        this.dataReceived = dataReceived;
        return this;
    }

    @Override
    public String toString()
    {
        return "MessageReceived{" +
               "dataReceived=" + dataReceived +
               '}';
    }

    @Override
    public MessageReceived copy()
    {
        return new MessageReceived().set(dataReceived);
    }
}

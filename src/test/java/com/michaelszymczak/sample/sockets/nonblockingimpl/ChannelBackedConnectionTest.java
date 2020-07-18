package com.michaelszymczak.sample.sockets.nonblockingimpl;

import com.michaelszymczak.sample.sockets.api.ConnectionIdValue;
import com.michaelszymczak.sample.sockets.connection.ConnectionConfiguration;
import com.michaelszymczak.sample.sockets.support.ConnectionEventsSpy;
import com.michaelszymczak.sample.sockets.support.FakeChannel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelBackedConnectionTest
{
    @Test
    void shouldNotBeClosedInitially()
    {
        assertThat(newConnection().isClosed()).isFalse();
    }

    @Test
    void shouldReadConfiguration()
    {
        final ChannelBackedConnection connection = newConnection(new ConnectionConfiguration(new ConnectionIdValue(8080, 51), 9090, 10));

        assertThat(connection.connectionId()).isEqualTo(51);
        assertThat(connection.port()).isEqualTo(8080);
    }

    private ChannelBackedConnection newConnection()
    {
        return newConnection(config());
    }

    private ChannelBackedConnection newConnection(final ConnectionConfiguration config)
    {
        return new ChannelBackedConnection(config, channel(), eventsListener());
    }

    private ConnectionEventsSpy eventsListener()
    {
        return new ConnectionEventsSpy();
    }

    private FakeChannel channel()
    {
        return new FakeChannel();
    }

    private ConnectionConfiguration config()
    {
        return new ConnectionConfiguration(new ConnectionIdValue(8080, 51), 9090, 10);
    }

}
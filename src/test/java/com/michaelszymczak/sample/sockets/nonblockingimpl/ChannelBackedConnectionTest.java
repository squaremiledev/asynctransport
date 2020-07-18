package com.michaelszymczak.sample.sockets.nonblockingimpl;

import com.michaelszymczak.sample.sockets.api.CommandId;
import com.michaelszymczak.sample.sockets.api.ConnectionIdValue;
import com.michaelszymczak.sample.sockets.api.commands.SendData;
import com.michaelszymczak.sample.sockets.api.events.ConnectionEvent;
import com.michaelszymczak.sample.sockets.api.events.DataSent;
import com.michaelszymczak.sample.sockets.connection.ConnectionConfiguration;
import com.michaelszymczak.sample.sockets.support.ConnectionEventsSpy;
import com.michaelszymczak.sample.sockets.support.FakeChannel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static com.michaelszymczak.sample.sockets.support.Assertions.assertEqual;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Collections.singletonList;

class ChannelBackedConnectionTest
{

    private ConnectionEventsSpy events = new ConnectionEventsSpy();

    @Test
    void shouldNotBeClosedInitially()
    {
        assertThat(newConnection().isClosed()).isFalse();
        assertThat(events.all(ConnectionEvent.class)).isEmpty();
    }

    @Test
    void shouldReadConfiguration()
    {
        final ChannelBackedConnection connection = newConnection(new ConnectionConfiguration(new ConnectionIdValue(8080, 51), 9090, 10));

        assertThat(connection.connectionId()).isEqualTo(51);
        assertThat(connection.port()).isEqualTo(8080);
        assertThat(events.all(ConnectionEvent.class)).isEmpty();
    }

    @Test
    void shouldIncludeConnectionIdInTheEvent()
    {
        final ChannelBackedConnection connection = newConnection(new ConnectionConfiguration(new ConnectionIdValue(1234, 3), 5678, 7));

        // When
        connection.handle(connection.command(SendData.class));

        // Then
        assertThat(events.last(DataSent.class).connectionId()).isEqualTo(3);
        assertThat(events.last(DataSent.class).port()).isEqualTo(1234);
        assertThat(events.last(DataSent.class).commandId()).isEqualTo(CommandId.NO_COMMAND_ID);
    }

    @Test
    void shouldIncludeCommandIdInTheEventIfMentioned()
    {
        final ChannelBackedConnection connection = newConnection();

        // When
        connection.handle(connection.command(SendData.class).set(new byte[0], 52));

        // Then
        assertThat(events.last(DataSent.class).commandId()).isEqualTo(52);

        // When
        connection.handle(connection.command(SendData.class).set(new byte[0]));

        // Then
        assertThat(events.last(DataSent.class).commandId()).isEqualTo(CommandId.NO_COMMAND_ID);
    }

    @Test
    void shouldNotifyThatDidNotTryToSendAnyData()
    {
        final FakeChannel channel = new FakeChannel().maxBytesWrittenInOneGo(0);
        final ChannelBackedConnection connection = newConnection(channel);
        final SendData sendData = connection.command(SendData.class).set(new byte[0]);


        // When
        connection.handle(sendData);

        // Then
        assertEqual(events.all(DataSent.class), new DataSent(connection, 0, 0));
        assertTotalNumberOfEvents(1);
        assertThat(channel.attemptedToWrite()).isEqualTo(singletonList(""));
    }

    @Test
    void shouldNotifyThatDidNotManageToSendAnyData()
    {
        final FakeChannel channel = new FakeChannel().maxBytesWrittenInOneGo(0);
        final ChannelBackedConnection connection = newConnection(channel);
        final SendData sendData = connection.command(SendData.class).set("foo".getBytes(US_ASCII));

        // When
        connection.handle(sendData);

        // Then
        assertEqual(events.all(DataSent.class), new DataSent(connection, 0, 0));
        assertTotalNumberOfEvents(1);
        assertThat(channel.attemptedToWrite()).isEqualTo(singletonList(""));
    }

    private void assertTotalNumberOfEvents(final int expected)
    {
        assertThat(events.all(ConnectionEvent.class)).hasSize(expected);
    }

    private ChannelBackedConnection newConnection()
    {
        return newConnection(config());
    }

    private ChannelBackedConnection newConnection(final FakeChannel channel)
    {
        return new ChannelBackedConnection(config(), channel, events);
    }

    private ChannelBackedConnection newConnection(final ConnectionConfiguration config)
    {
        return new ChannelBackedConnection(config, new FakeChannel(), events);
    }

    private ConnectionConfiguration config()
    {
        return new ConnectionConfiguration(new ConnectionIdValue(8080, 51), 9090, 10);
    }

}
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
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class ChannelBackedConnectionTest
{

    private ConnectionEventsSpy events = new ConnectionEventsSpy();

    private static byte[] bytes(final String s)
    {
        return s.getBytes(US_ASCII);
    }

    @Test
    void shouldNotBeClosedInitially()
    {
        assertThat(newConnection().isClosed()).isFalse();
        assertThat(events.all(ConnectionEvent.class)).isEmpty();
    }

    @Test
    void shouldReadConfiguration()
    {
        final ChannelBackedConnection connection = newConnection(new ConnectionConfiguration(new ConnectionIdValue(8080, 51), 9090, 10, 20));

        assertThat(connection.connectionId()).isEqualTo(51);
        assertThat(connection.port()).isEqualTo(8080);
        assertThat(events.all(ConnectionEvent.class)).isEmpty();
    }

    @Test
    void shouldIncludeConnectionIdInTheEvent()
    {
        final ChannelBackedConnection connection = newConnection(new ConnectionConfiguration(new ConnectionIdValue(1234, 3), 5678, 7, 7));

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
        assertThat(channel.attemptedToWrite()).isEqualTo(emptyList());
        assertEqual(events.all(DataSent.class), new DataSent(connection, 0, 0, 0));
        assertTotalNumberOfEvents(1);
    }

    @Test
    void shouldNotifyThatBufferedTheDataIfUnableToSendAtAll()
    {
        final FakeChannel channel = new FakeChannel().maxBytesWrittenInOneGo(0);
        final ChannelBackedConnection connection = newConnection(channel);
        final SendData sendData = connection.command(SendData.class).set(bytes("foo"));

        // When
        connection.handle(sendData);

        // Then
        assertThat(channel.attemptedToWrite()).isEqualTo(singletonList(""));
        assertEqual(events.all(DataSent.class), new DataSent(connection, 0, 0, 3));
        assertTotalNumberOfEvents(1);
    }

    @Test
    void shouldAttemptToSendAllDataInOneGo()
    {
        final FakeChannel channel = new FakeChannel().allBytesWrittenInOneGo();
        final ChannelBackedConnection connection = newConnection(channel);
        final byte[] content = bytes("fooBAR");
        final SendData sendData = connection.command(SendData.class).set(content);

        // When
        connection.handle(sendData);

        // Then
        assertThat(channel.attemptedToWrite()).isEqualTo(singletonList("fooBAR"));
        assertEqual(events.all(DataSent.class), new DataSent(connection, content.length, content.length, content.length));
        assertTotalNumberOfEvents(1);
    }

    @Test
    void shouldSendAsMuchAsPossibleInOneGoAndBufferTheRest()
    {
        final FakeChannel channel = new FakeChannel().maxBytesWrittenInOneGo(3);
        final ChannelBackedConnection connection = newConnection(channel);
        final byte[] content = bytes("fooBARba");
        final SendData sendData = connection.command(SendData.class).set(content);

        // When
        connection.handle(sendData);

        // Then
        assertThat(channel.attemptedToWrite()).isEqualTo(singletonList("foo"));
        assertEqual(events.all(DataSent.class), new DataSent(connection, 3, 3, 8));
        assertTotalNumberOfEvents(1);
    }

    @Test
    void shouldSendAllBufferedDataIfPossible()
    {
        final FakeChannel channel = new FakeChannel().maxBytesWrittenInOneGo(3);
        final ChannelBackedConnection connection = newConnection(channel);
        connection.handle(connection.command(SendData.class).set(bytes("fooBA")));
        assertEqual(events.all(DataSent.class), new DataSent(connection, 3, 3, 5));

        // When
        connection.handle(connection.command(SendData.class).set(new byte[0]));


        // Then
        assertThat(channel.attemptedToWrite()).isEqualTo(asList("foo", "BA"));
        assertTotalNumberOfEvents(2);
        assertEqual(events.all(DataSent.class), new DataSent(connection, 3, 3, 5), new DataSent(connection, 2, 5, 5));
    }

    @Test
    void shouldSendAsMuchOfBufferedDataAsPossible()
    {
        final FakeChannel channel = new FakeChannel().maxBytesWrittenInOneGo(3);
        final ChannelBackedConnection connection = newConnection(channel);
        connection.handle(connection.command(SendData.class).set(bytes("fooBARba")));
        assertEqual(events.all(DataSent.class), new DataSent(connection, 3, 3, 8));

        // When
        connection.handle(connection.command(SendData.class).set(new byte[0]));


        // Then
        assertThat(channel.attemptedToWrite()).isEqualTo(asList("foo", "BAR"));
        assertTotalNumberOfEvents(2);
        assertEqual(events.all(DataSent.class), new DataSent(connection, 3, 3, 8), new DataSent(connection, 3, 6, 8));
    }

    @Test
    void shouldNotKeepSetDataWhenRetrievedAgain()
    {
        final FakeChannel channel = new FakeChannel().maxBytesWrittenInOneGo(3);
        final ChannelBackedConnection connection = newConnection(channel);
        final byte[] content = bytes("fooBAR");
        connection.command(SendData.class).set(content);

        // When
        connection.handle(connection.command(SendData.class));


        // Then
        assertThat(channel.attemptedToWrite()).isEqualTo(emptyList());
        assertEqual(events.all(DataSent.class), new DataSent(connection, 0, 0, 0));
        assertTotalNumberOfEvents(1);
    }

    @Test
    void shouldNotKeepUnsentDataWhenRetrievedAgain()
    {
        final FakeChannel channel = new FakeChannel().maxBytesWrittenInOneGo(3);
        final ChannelBackedConnection connection = newConnection(channel);
        connection.handle(connection.command(SendData.class).set(bytes("fooBAR")));
        assertEqual(events.all(DataSent.class), new DataSent(connection, 3, 3, 6));

        // When
        connection.handle(connection.command(SendData.class));

        // Then
        assertThat(channel.attemptedToWrite()).isEqualTo(asList("foo", "BAR"));
        assertEqual(events.all(DataSent.class), new DataSent(connection, 3, 3, 6), new DataSent(connection, 3, 6, 6));
        assertTotalNumberOfEvents(2);
    }

    @Test
    void shouldCalculateTotalDataSent()
    {
        final FakeChannel channel = new FakeChannel().allBytesWrittenInOneGo();
        final ChannelBackedConnection connection = newConnection(channel);

        // When
        connection.handle(connection.command(SendData.class).set(bytes("foo")));
        connection.handle(connection.command(SendData.class).set(bytes("bar")));

        // Then
        assertThat(channel.attemptedToWrite()).isEqualTo(asList("foo", "bar"));
        assertEqual(events.all(DataSent.class), new DataSent(connection, 3, 3, 3), new DataSent(connection, 3, 6, 6));
        assertTotalNumberOfEvents(2);
    }

    @Test
    void shouldEventuallyDrainTheBuffer()
    {
        final FakeChannel channel = new FakeChannel().maxBytesWrittenInOneGo(2);
        final ChannelBackedConnection connection = newConnection(channel);
        connection.handle(connection.command(SendData.class).set(bytes("0123456"), 100));
        assertEqual(events.all(DataSent.class), new DataSent(connection, 2, 2, 7, 100));

        // When
        connection.handle(connection.command(SendData.class).set(new byte[0], 101));
        connection.handle(connection.command(SendData.class).set(new byte[0], 102));
        connection.handle(connection.command(SendData.class).set(new byte[0], 103));
        connection.handle(connection.command(SendData.class).set(new byte[0], 104));

        // Then
        assertThat(channel.attemptedToWrite()).isEqualTo(asList("01", "23", "45", "6"));
        assertTotalNumberOfEvents(5);
        assertEqual(
                events.all(DataSent.class),
                new DataSent(connection, 2, 2, 7, 100),
                new DataSent(connection, 2, 4, 7, 101),
                new DataSent(connection, 2, 6, 7, 102),
                new DataSent(connection, 1, 7, 7, 103),
                new DataSent(connection, 0, 7, 7, 104)
        );
    }

    @Test
    void shouldSendAlsoDataFromTheCurrentCommandIfWholeBufferDrained()
    {
        final FakeChannel channel = new FakeChannel().maxBytesWrittenInOneGo(5);
        final ChannelBackedConnection connection = newConnection(channel);
        connection.handle(connection.command(SendData.class).set(bytes("1234567"), 100));
        assertEqual(events.all(DataSent.class), new DataSent(connection, 5, 5, 7, 100));

        // When
        connection.handle(connection.command(SendData.class).set(bytes("89"), 101));

        // Then
        assertThat(channel.attemptedToWrite()).isEqualTo(asList("12345", "67", "89"));
        assertTotalNumberOfEvents(2);
        assertEqual(
                events.all(DataSent.class),
                new DataSent(connection, 5, 5, 7, 100),
                new DataSent(connection, 4, 9, 9, 101)
        );
    }

    @Test
    void shouldSendAsMuchDataAsPossibleAndBufferRestIfWholeBufferDrained()
    {
        final FakeChannel channel = new FakeChannel().maxBytesWrittenInOneGo(5);
        final ChannelBackedConnection connection = newConnection(channel);
        connection.handle(connection.command(SendData.class).set(bytes("1234567"), 100));
        assertEqual(events.all(DataSent.class), new DataSent(connection, 5, 5, 7, 100));

        // When
        connection.handle(connection.command(SendData.class).set(bytes("8901234"), 101));
        connection.handle(connection.command(SendData.class).set(bytes("5"), 102));

        // Then
        assertThat(channel.attemptedToWrite()).isEqualTo(asList("12345", "67", "89012", "34", "5"));
        assertTotalNumberOfEvents(3);
        assertEqual(
                events.all(DataSent.class),
                new DataSent(connection, 5, 5, 7, 100),
                new DataSent(connection, 7, 12, 14, 101),
                new DataSent(connection, 3, 15, 15, 102)
        );
    }

    @Test
    void shouldNotSendAnyNewDataUntilBufferedDataSent()
    {
        final FakeChannel channel = new FakeChannel().maxBytesWrittenInOneGo(2);
        final ChannelBackedConnection connection = newConnection(channel);
        connection.handle(connection.command(SendData.class).set(bytes("012345"), 100));
        assertEqual(events.all(DataSent.class), new DataSent(connection, 2, 2, 6, 100));

        // When
        connection.handle(connection.command(SendData.class).set(bytes("67"), 101));
        connection.handle(connection.command(SendData.class).set(bytes("8"), 102));
        connection.handle(connection.command(SendData.class).set(new byte[0], 103));
        connection.handle(connection.command(SendData.class).set(new byte[0], 104));
        connection.handle(connection.command(SendData.class).set(new byte[0], 105));

        // Then
        assertThat(channel.attemptedToWrite()).isEqualTo(asList("01", "23", "45", "67", "8"));
        assertTotalNumberOfEvents(6);
        assertEqual(
                events.all(DataSent.class),
                new DataSent(connection, 2, 2, 6, 100),
                new DataSent(connection, 2, 4, 8, 101),
                new DataSent(connection, 2, 6, 9, 102),
                new DataSent(connection, 2, 8, 9, 103),
                new DataSent(connection, 1, 9, 9, 104),
                new DataSent(connection, 0, 9, 9, 105)
        );
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
        return new ConnectionConfiguration(new ConnectionIdValue(8080, 51), 9090, 10, 20);
    }
}
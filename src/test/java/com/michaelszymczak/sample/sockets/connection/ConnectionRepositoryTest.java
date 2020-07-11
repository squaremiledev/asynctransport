package com.michaelszymczak.sample.sockets.connection;

import com.michaelszymczak.sample.sockets.api.commands.ConnectionCommand;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionRepositoryTest
{
    @Test
    void shouldBeEmptyInitially()
    {
        assertThat(new ConnectionRepository().size()).isEqualTo(0);
    }

    @Test
    void shouldAddConnectionToTheRepository()
    {
        final ConnectionAggregate connection = new SampleConnection(5544, 2);
        final ConnectionRepository repository = new ConnectionRepository();

        // When
        repository.add(connection);

        // Then
        assertThat(repository.size()).isEqualTo(1);
        assertThat(repository.findByConnectionId(2)).usingRecursiveComparison().isEqualTo(connection);
    }

    @Test
    void shouldFindConnectionById()
    {
        final ConnectionRepository repository = new ConnectionRepository();
        repository.add(new SampleConnection(5542, 2));
        repository.add(new SampleConnection(5543, 3));
        repository.add(new SampleConnection(5544, 4));

        // Then
        assertThat(repository.findByConnectionId(3)).usingRecursiveComparison().isEqualTo((ConnectionAggregate)new SampleConnection(5543, 3));
        assertThat(repository.contains(3)).isTrue();
        assertThat(repository.findByConnectionId(5)).isNull();
        assertThat(repository.contains(5)).isFalse();
    }

    @Test
    void shouldCloseAllConnectionWhenClosed()
    {
        final ConnectionRepository repository = new ConnectionRepository();
        final SampleConnection connection1 = new SampleConnection(5541, 1);
        final SampleConnection connection2 = new SampleConnection(5542, 2);
        final SampleConnection connection3 = new SampleConnection(5543, 3);
        repository.add(connection1);
        repository.add(connection2);
        repository.add(connection3);
        assertThat(connection1.isClosed()).isFalse();
        assertThat(connection1.isClosed()).isFalse();
        assertThat(connection1.isClosed()).isFalse();

        // When
        repository.close();

        // Then
        assertThat(connection1.isClosed()).isTrue();
        assertThat(connection1.isClosed()).isTrue();
        assertThat(connection1.isClosed()).isTrue();
    }

    private static class SampleConnection implements ConnectionAggregate
    {

        private final int port;
        private final long connectionId;
        private boolean closed;

        public SampleConnection(final int port, final long connectionId)
        {
            this(port, connectionId, false);
        }

        public SampleConnection(final int port, final long connectionId, final boolean closed)
        {
            this.port = port;
            this.connectionId = connectionId;
            this.closed = closed;
        }

        @Override
        public int port()
        {
            return port;
        }

        @Override
        public long connectionId()
        {
            return connectionId;
        }

        @Override
        public void handle(final ConnectionCommand command)
        {

        }

        public boolean isClosed()
        {
            return closed;
        }

        @Override
        public void close()
        {
            closed = true;
        }
    }
}
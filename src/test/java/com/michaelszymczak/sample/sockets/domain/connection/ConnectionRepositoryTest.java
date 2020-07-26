package com.michaelszymczak.sample.sockets.domain.connection;

import com.michaelszymczak.sample.sockets.domain.api.commands.ConnectionCommand;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

class ConnectionRepositoryTest
{
    private final RepositoryUpdatesSpy repositoryUpdates = new RepositoryUpdatesSpy();

    @Test
    void shouldBeEmptyInitially()
    {
        assertThat(new ConnectionRepository(repositoryUpdates).size()).isEqualTo(0);
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEmpty();
    }

    @Test
    void shouldAddConnectionToTheRepository()
    {
        final Connection connection = new SampleConnection(5544, 2);
        final ConnectionRepository repository = new ConnectionRepository(repositoryUpdates);

        // When
        repository.add(connection);

        // Then
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(singletonList(1));
        assertThat(repository.size()).isEqualTo(1);
        assertThat(repository.findByConnectionId(2)).usingRecursiveComparison().isEqualTo(connection);
    }

    @Test
    void shouldPreventFromAddingConnectionTwice()
    {
        final Connection connection = new SampleConnection(5544, 2);
        final ConnectionRepository repository = new ConnectionRepository(repositoryUpdates);
        repository.add(connection);
        assertThat(repository.size()).isEqualTo(1);
        assertThat(repository.contains(2)).isTrue();
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(singletonList(1));

        // When
        assertThrows(IllegalStateException.class, () -> repository.add(new SampleConnection(9999, 2)));
        assertThrows(IllegalStateException.class, () -> repository.add(new SampleConnection(5544, 2)));

        // Then
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(singletonList(1));
        assertThat(repository.size()).isEqualTo(1);
        assertThat(repository.contains(2)).isTrue();
        assertThat(repository.contains(3)).isFalse();
    }

    @Test
    void shouldAllowReAddTheSameConnectionAfterRemoved() throws Exception
    {
        final ConnectionRepository repository = new ConnectionRepository(repositoryUpdates);
        repository.add(new SampleConnection(5544, 2));
        repository.findByConnectionId(2).close();
        repository.removeById(2);
        assertThat(repository.size()).isEqualTo(0);
        assertThat(repository.contains(2)).isFalse();
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(asList(1, 0));

        // When
        repository.add(new SampleConnection(5544, 2));

        // Then
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(asList(1, 0, 1));
        assertThat(repository.size()).isEqualTo(1);
        assertThat(repository.contains(2)).isTrue();
    }

    @Test
    void shouldAllowReAddTheSameConnectionAfterCleared()
    {
        final ConnectionRepository repository = new ConnectionRepository(repositoryUpdates);
        repository.add(new SampleConnection(5544, 2));
        repository.close();
        assertThat(repository.size()).isEqualTo(0);
        assertThat(repository.contains(2)).isFalse();
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(asList(1, 0));

        // When
        repository.add(new SampleConnection(5544, 2));

        // Then
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(asList(1, 0, 1));
        assertThat(repository.size()).isEqualTo(1);
        assertThat(repository.contains(2)).isTrue();
    }

    @Test
    void shouldFindConnectionById()
    {
        final ConnectionRepository repository = new ConnectionRepository(repositoryUpdates);
        repository.add(new SampleConnection(5542, 2));
        repository.add(new SampleConnection(5543, 3));
        repository.add(new SampleConnection(5544, 4));

        // Then
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(asList(1, 2, 3));
        assertThat(repository.findByConnectionId(3)).usingRecursiveComparison().isEqualTo(new SampleConnection(5543, 3));
        assertThat(repository.contains(3)).isTrue();
        assertThat(repository.findByConnectionId(5)).isNull();
        assertThat(repository.contains(5)).isFalse();
    }

    @Test
    void shouldCloseAllConnectionWhenClosed()
    {
        final ConnectionRepository repository = new ConnectionRepository(repositoryUpdates);
        final SampleConnection connection1 = new SampleConnection(5541, 1);
        final SampleConnection connection2 = new SampleConnection(5542, 2);
        final SampleConnection connection3 = new SampleConnection(5543, 3);
        repository.add(connection1);
        repository.add(connection2);
        repository.add(connection3);
        assertThat(connection1.state()).isNotEqualTo(ConnectionState.CLOSED);
        assertThat(connection2.state()).isNotEqualTo(ConnectionState.CLOSED);
        assertThat(connection3.state()).isNotEqualTo(ConnectionState.CLOSED);
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(asList(1, 2, 3));

        // When
        repository.close();

        // Then
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(asList(1, 2, 3, 0));
        assertThat(connection1.state()).isEqualTo(ConnectionState.CLOSED);
        assertThat(connection2.state()).isEqualTo(ConnectionState.CLOSED);
        assertThat(connection3.state()).isEqualTo(ConnectionState.CLOSED);
    }

    @Test
    void shouldRemoveConnections()
    {
        final ConnectionRepository repository = new ConnectionRepository(repositoryUpdates);
        final SampleConnection connection1 = new SampleConnection(5541, 1);
        final SampleConnection connection2 = new SampleConnection(5542, 2);
        final SampleConnection connection3 = new SampleConnection(5543, 3);
        connection1.close();
        connection2.close();
        connection3.close();
        repository.add(connection1);
        repository.add(connection2);
        repository.add(connection3);
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(asList(1, 2, 3));

        // When
        repository.removeById(2);

        // Then
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(asList(1, 2, 3, 2));
        assertThat(repository.contains(1)).isTrue();
        assertThat(repository.contains(2)).isFalse();
        assertThat(repository.contains(3)).isTrue();
    }

    @Test
    void shouldNotAllowRemovingUnclosedConnection()
    {
        final ConnectionRepository repository = new ConnectionRepository(repositoryUpdates);
        final SampleConnection connection = new SampleConnection(5542, 2);
        assertThat(connection.state()).isNotEqualTo(ConnectionState.CLOSED);
        repository.add(connection);
        assertThat(repository.contains(2)).isTrue();
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(singletonList(1));

        // When
        assertThrows(IllegalStateException.class, () -> repository.removeById(2));

        // Then
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(singletonList(1));
        assertThat(repository.contains(2)).isTrue();
    }

    @Test
    void shouldIgnoreRemovalOfNonExistingConnection()
    {
        final ConnectionRepository repository = new ConnectionRepository(repositoryUpdates);
        repository.add(new SampleConnection(5542, 1));
        assertThat(repository.size()).isEqualTo(1);
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(singletonList(1));

        // When
        repository.removeById(123);

        // Then
        assertThat(repositoryUpdates.numberOfConnectionsChangedUpdates()).isEqualTo(singletonList(1));
        assertThat(repository.size()).isEqualTo(1);
    }

    private static class SampleConnection implements Connection
    {

        private final int port;
        private final long connectionId;
        private boolean closed;
        private ConnectionState state;

        public SampleConnection(final int port, final long connectionId)
        {
            this.port = port;
            this.connectionId = connectionId;
            this.closed = false;
            this.state = ConnectionState.NO_OUTSTANDING_DATA;
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
        public boolean handle(final ConnectionCommand command)
        {
            return true;
        }

        @Override
        public <C extends ConnectionCommand> C command(final Class<C> commandType)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ConnectionState state()
        {
            return state;
        }

        @Override
        public void accepted(final long commandIdThatTriggeredListening)
        {

        }

        @Override
        public void connected(final long commandId)
        {

        }

        @Override
        public void close()
        {
            closed = true;
            state = ConnectionState.CLOSED;
        }
    }
}
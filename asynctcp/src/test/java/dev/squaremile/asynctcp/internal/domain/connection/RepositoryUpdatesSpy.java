package dev.squaremile.asynctcp.internal.domain.connection;

import java.util.ArrayList;
import java.util.List;

public class RepositoryUpdatesSpy implements ConnectionRepository.RepositoryUpdates
{
    private final List<Integer> numberOfConnectionsChangedUpdates = new ArrayList<>();

    @Override
    public void onNumberOfConnectionsChanged(final int newNumberOfConnections)
    {
        numberOfConnectionsChangedUpdates.add(newNumberOfConnections);
    }

    public List<Integer> numberOfConnectionsChangedUpdates()
    {
        return numberOfConnectionsChangedUpdates;
    }
}

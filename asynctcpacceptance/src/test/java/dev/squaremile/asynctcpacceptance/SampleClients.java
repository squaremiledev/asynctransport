package dev.squaremile.asynctcpacceptance;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.agrona.CloseHelper;

public class SampleClients implements AutoCloseable
{
    private final Map<Integer, SampleClient> clients = new HashMap<>();

    public SampleClients()
    {
        clients.put(1, new SampleClient());
        clients.put(2, new SampleClient());
        clients.put(3, new SampleClient());
        clients.put(4, new SampleClient());
    }

    public SampleClient client(int clientNumber)
    {
        return Optional.ofNullable(clients.get(clientNumber)).orElseThrow(RuntimeException::new);
    }

    @Override
    public void close()
    {
        clients.values().forEach(CloseHelper::close);
    }
}

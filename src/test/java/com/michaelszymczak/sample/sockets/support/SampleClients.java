package com.michaelszymczak.sample.sockets.support;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SampleClients implements AutoCloseable
{
    private final Map<Integer, SampleClient> clients = new HashMap<>();

    public SampleClients() throws SocketException
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
        clients.values().forEach(Resources::close);
    }
}

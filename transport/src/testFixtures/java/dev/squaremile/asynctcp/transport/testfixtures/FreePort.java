package dev.squaremile.asynctcp.transport.testfixtures;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;


import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

public class FreePort
{
    public static int freePort()
    {
        return freePort(0);
    }

    public static int freePort(final int port)
    {
        try (ServerSocket socket = new ServerSocket(port))
        {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static int freePortOtherThan(final Integer... reservedPorts)
    {
        final List<Integer> reserved = Arrays.asList(reservedPorts);
        int newPort;
        do
        {
            newPort = freePort(0);
        }
        while (reserved.contains(newPort));
        return newPort;
    }

    public static Map<String, List<Integer>> freePortPools(final String... requestedPools)
    {
        final Deque<Integer> freePortsDeque = new ArrayDeque<>(freePorts(stream(requestedPools).mapToInt(r1 -> parseInt(r1.split(":")[1])).sum()));
        return stream(requestedPools).collect(toMap(
                r -> r.split(":")[0],
                r ->
                {
                    int numberOfPorts = parseInt(r.split(":")[1]);
                    List<Integer> freePorts = new ArrayList<>(numberOfPorts);
                    for (int i = 0; i < numberOfPorts; i++)
                    {
                        freePorts.add(freePortsDeque.poll());
                    }
                    return freePorts;
                }
        ));
    }

    private static List<Integer> freePorts(final int count)
    {
        final Integer[] allocatedPorts = new Integer[count];
        for (int i = 0; i < count; i++)
        {
            allocatedPorts[i] = freePortOtherThan(allocatedPorts);
        }
        return Arrays.asList(allocatedPorts);
    }
}

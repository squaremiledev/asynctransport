package dev.squaremile.trcheck.standalone;


import dev.squaremile.transport.aerontcpgateway.api.AeronTcp;
import dev.squaremile.transport.aerontcpgateway.api.TcpDriver;

class DriverApplication
{
    public static void start(final String directory)
    {
        try (final TcpDriver tcpDriver = startDriver(directory))
        {
            System.out.println("Driver started with configuration: " + tcpDriver.configuration());
            while (!Thread.interrupted())
            {
                tcpDriver.work();
            }
        }
    }

    private static TcpDriver startDriver(final String directory)
    {
        return (
                directory.isEmpty() ?
                new AeronTcp().createEmbeddedTcpDriver(10, 11) :
                new AeronTcp().createTcpDriver(10, 11, true, directory)
        ).start();
    }
}

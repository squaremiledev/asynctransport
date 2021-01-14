package dev.squaremile.trcheck.standalone;

import static java.lang.Integer.parseInt;

public class TrCheck
{
    public static void main(String[] args)
    {
        if (args.length < 2 || !"ping".equals(args[0]) || (!"server".equals(args[1]) && !"client".equals(args[1])))
        {
            System.out.println("Usage: trcheck ping server|client options...");
            System.exit(1);
        }
        else if ("server".equals(args[1]))
        {
            if (args.length != 3)
            {
                System.out.println("Usage: trcheck ping server port");
                System.out.println("e.g.   trcheck ping server 8889");
                System.exit(1);
            }
            int port = parseInt(args[2]);
            EchoApplication.start(port, () -> System.out.println("Listening on port " + port));
            System.exit(0);
        }
        else
        {
            if (args.length != 9)
            {
                System.out.println("Usage: trcheck ping client remoteHost remotePort secondsWarmUp secondsRun sendingRatePerSecond respondToNth extraDataLength");
                System.out.println("e.g.   trcheck ping client localhost        8889            40         50               400000          100               0");
                System.exit(1);
            }

            TcpPingConfiguration configuration = new TcpPingConfiguration.Builder()
                    .remoteHost(args[2])
                    .remotePort(parseInt(args[3]))
                    .secondsWarmUp(parseInt(args[4]))
                    .secondsRun(parseInt(args[5]))
                    .sendingRatePerSecond(parseInt(args[6]))
                    .respondToNth(parseInt(args[7]))
                    .extraDataLength(parseInt(args[8]))
                    .create();
            System.out.println(configuration);
            SourcingConnectionApplication.runPing(configuration).printResults();
            System.exit(0);
        }
    }
}

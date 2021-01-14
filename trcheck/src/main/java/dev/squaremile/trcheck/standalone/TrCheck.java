package dev.squaremile.trcheck.standalone;

import static java.util.Arrays.stream;

public class TrCheck
{
    public static void main(String[] args)
    {
        if (args.length < 1 || !"benchmark".equals(args[0]))
        {
            System.out.println("Usage: trcheck benchmark modes...");
            System.exit(1);
        }
        else
        {
            Benchmark.main(stream(args).skip(1).toArray(String[]::new));
        }
    }

}

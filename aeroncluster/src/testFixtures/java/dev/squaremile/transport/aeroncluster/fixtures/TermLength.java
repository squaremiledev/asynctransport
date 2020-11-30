package dev.squaremile.transport.aeroncluster.fixtures;

public class TermLength
{
    private final int lengthInKB;

    public TermLength(int lengthInKB)
    {
        this.lengthInKB = lengthInKB;
    }

    public int asBytes()
    {
        return lengthInKB * 1024;
    }

    public String asChannelParameter()
    {
        return lengthInKB + "k";
    }
}

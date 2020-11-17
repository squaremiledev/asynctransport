package dev.squaremile.asynctcp.fix;

public class FixMetadata
{
    private final String username;
    private final String fixVersion;


    public FixMetadata(final String username, final String fixVersion)
    {
        this.username = username;
        this.fixVersion = fixVersion;
    }

    public String username()
    {
        return username;
    }

    public String fixVersion()
    {
        return fixVersion;
    }
}

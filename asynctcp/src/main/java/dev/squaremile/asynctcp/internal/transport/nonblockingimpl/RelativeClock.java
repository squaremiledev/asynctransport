package dev.squaremile.asynctcp.internal.transport.nonblockingimpl;

public interface RelativeClock
{
    long relativeNanoTime();

    class SystemRelativeClock implements RelativeClock
    {
        @Override
        public long relativeNanoTime()
        {
            return System.nanoTime();
        }
    }
}

package dev.squaremile.asynctcp.transport.internal.nonblockingimpl;

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

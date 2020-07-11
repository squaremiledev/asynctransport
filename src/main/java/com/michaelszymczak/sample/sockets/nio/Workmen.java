package com.michaelszymczak.sample.sockets.nio;

public class Workmen
{
    public static BlockingWorkman rethrowing(final ThrowingBlockingWorkman workman)
    {
        return () ->
        {
            try
            {
                workman.work();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        };
    }

    public interface NonBlockingWorkman
    {
        void work();
    }

    public interface ThrowingNonBlockingWorkman
    {
        void work() throws Exception;
    }

    public interface BlockingWorkman
    {
        void work();
    }

    public interface ThrowingBlockingWorkman
    {
        void work() throws Exception;
    }
}

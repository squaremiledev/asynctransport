package com.michaelszymczak.sample.sockets.impl;

public class Resources
{
    public static void close(final AutoCloseable resource)
    {
        if (resource != null)
        {
            try
            {
                resource.close();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}

package com.michaelszymczak.sample.sockets.support;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadSafeReadDataSpy implements SampleClient.ReadDataConsumer
{
    private final AtomicReference<byte[]> dataHolder = new AtomicReference<>();

    @Override
    public void consume(final byte[] data, final int length)
    {
        dataHolder.set(Arrays.copyOf(data, length));
    }

    public byte[] dataRead()
    {
        return dataHolder.get();
    }
}

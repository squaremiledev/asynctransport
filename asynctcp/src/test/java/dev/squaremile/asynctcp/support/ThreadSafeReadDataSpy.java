package dev.squaremile.asynctcp.support;

public class ThreadSafeReadDataSpy implements SampleClient.ReadDataConsumer
{
    private byte[] dataHolder = new byte[0];

    @Override
    public synchronized void consume(final byte[] data, final int length)
    {
        byte[] previousData = this.dataHolder;
        byte[] newData = new byte[previousData.length + length];
        System.arraycopy(previousData, 0, newData, 0, previousData.length);
        System.arraycopy(data, 0, newData, previousData.length, length);
        this.dataHolder = newData;
    }

    public synchronized byte[] dataRead()
    {
        return dataHolder;
    }
}

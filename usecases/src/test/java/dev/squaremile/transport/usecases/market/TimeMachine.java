package dev.squaremile.transport.usecases.market;

import java.util.Arrays;
import java.util.function.LongConsumer;

public class TimeMachine
{
    public long time;

    public TimeMachine(final long time)
    {
        this.time = time;
    }

    public void tick(final LongConsumer... happenstances)
    {
        tick(1, happenstances);
    }

    public void tick(final int timesTicked, final LongConsumer... happenstances)
    {
        for (int i = 0; i < timesTicked; i++)
        {
            Arrays.stream(happenstances).forEachOrdered(happenstance -> happenstance.accept(time++));
        }
    }

    public long time()
    {
        return time;
    }
}

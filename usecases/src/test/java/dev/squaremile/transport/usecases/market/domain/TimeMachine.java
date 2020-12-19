package dev.squaremile.transport.usecases.market.domain;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;

public class TimeMachine
{
    private static final long ONE_MILLISECOND_IN_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
    public long timeNs;

    public void tick(final LongConsumer... happenstances)
    {
        tick(1, happenstances);
    }

    public void tick(final int timesTicked, final LongConsumer... happenstances)
    {
        for (int i = 0; i < timesTicked; i++)
        {
            Arrays.stream(happenstances).forEachOrdered(happenstance ->
                                                        {
                                                            happenstance.accept(timeNs);
                                                            timeNs += ONE_MILLISECOND_IN_NANOS;
                                                        });
        }
    }

    public long time()
    {
        return timeNs;
    }
}

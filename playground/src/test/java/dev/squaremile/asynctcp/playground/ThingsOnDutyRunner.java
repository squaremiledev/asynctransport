package dev.squaremile.asynctcp.playground;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;


import dev.squaremile.asynctcp.api.app.OnDuty;

import static java.util.Arrays.asList;

class ThingsOnDutyRunner
{
    private final List<OnDuty> onDuty;
    private final BufferWriteSpy networkToUserWrites;
    private final BufferWriteSpy userToNetworkWrites;

    public ThingsOnDutyRunner(
            final BufferWriteSpy networkToUserWrites,
            final BufferWriteSpy userToNetworkWrites,
            final OnDuty... thingsOnDuty
    )
    {
        this.onDuty = asList(thingsOnDuty);
        this.networkToUserWrites = networkToUserWrites;
        this.userToNetworkWrites = userToNetworkWrites;
    }

    public BooleanSupplier reachedMinimumNetworkToUserWritesOf(final int value)
    {
        return getAsBoolean(value, 0);
    }

    public BooleanSupplier reachedMinimumUserToNetworkWritesOf(final int value)
    {
        return getAsBoolean(0, value);
    }


    private BooleanSupplier getAsBoolean(final int minimumNetworkToUserWrites, final int minimumUserToNetworkWrites)
    {
        return () ->
        {
            onDuty.forEach(OnDuty::work);
            return networkToUserWrites.entries().size() >= minimumNetworkToUserWrites &&
                   userToNetworkWrites.entries.size() >= minimumUserToNetworkWrites;
        };
    }

    public static class BufferWriteSpy
    {
        private final List<BufferWriteSpy.WrittenEntries> entries = new ArrayList<>();

        void add(final int offset, final int length)
        {
            entries.add(new BufferWriteSpy.WrittenEntries(offset, length));
        }

        List<BufferWriteSpy.WrittenEntries> entries()
        {
            return entries;
        }

        BufferWriteSpy.WrittenEntries entry(final int index)
        {
            return entries.get(index);
        }

        static class WrittenEntries
        {
            final int offset;
            final int length;

            WrittenEntries(final int offset, final int length)
            {
                this.offset = offset;
                this.length = length;
            }
        }
    }
}

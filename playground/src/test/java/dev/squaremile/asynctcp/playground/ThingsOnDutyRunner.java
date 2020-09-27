package dev.squaremile.asynctcp.playground;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;


import dev.squaremile.asynctcp.api.app.OnDuty;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

class ThingsOnDutyRunner
{
    private final List<OnDuty> onDuty;

    public ThingsOnDutyRunner(final OnDuty... thingsOnDuty)
    {
        this.onDuty = asList(thingsOnDuty);
    }

    public BooleanSupplier reached(final BooleanSupplier... conditions)
    {
        return () ->
        {
            onDuty.forEach(OnDuty::work);
            return stream(conditions).allMatch(BooleanSupplier::getAsBoolean);
        };
    }

    public static class BufferWriteSpy
    {
        private final List<BufferWriteSpy.WrittenEntries> entries = new ArrayList<>();

        void add(final int offset, final int length)
        {
            entries.add(new BufferWriteSpy.WrittenEntries(offset, length));
        }

        public int count()
        {
            return entries.size();
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

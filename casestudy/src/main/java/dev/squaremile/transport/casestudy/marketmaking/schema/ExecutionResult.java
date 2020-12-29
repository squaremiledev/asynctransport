/* Generated SBE (Simple Binary Encoding) message codec */
package dev.squaremile.transport.casestudy.marketmaking.schema;

public enum ExecutionResult
{
    NOT_EXECUTED((byte)48),

    EXECUTED((byte)49),

    /**
     * To be used to represent not present or null.
     */
    NULL_VAL((byte)0);

    private final byte value;

    ExecutionResult(final byte value)
    {
        this.value = value;
    }

    public byte value()
    {
        return value;
    }

    public static ExecutionResult get(final byte value)
    {
        switch (value)
        {
            case 48: return NOT_EXECUTED;
            case 49: return EXECUTED;
            case 0: return NULL_VAL;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}

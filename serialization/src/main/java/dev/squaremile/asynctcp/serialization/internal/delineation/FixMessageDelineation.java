package dev.squaremile.asynctcp.serialization.internal.delineation;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class FixMessageDelineation implements DelineationHandler
{
    private static final char SOH = '\u0001';
    private static final int CHECKSUM_SIZE = 7;
    private final DelineationHandler delineatedDataHandler;
    private LengthScanState lengthScanState = LengthScanState.START;
    private State state = State.LENGTH_UNKNOWN;
    private int declaredLength;
    private int bytesBeforeDeclaredLength;
    private int unprocessedLength;
    // TODO #16 may not be enough
    private final MutableDirectBuffer unprocessed = new UnsafeBuffer(new byte[1024]);

    public FixMessageDelineation(final DelineationHandler delineatedDataHandler)
    {
        this.delineatedDataHandler = delineatedDataHandler;
    }

    @Override
    public void onData(final DirectBuffer buffer, final int offset, final int length)
    {
        int bytesProcessedInThisRun = 0;
        do
        {
            if (state == State.LENGTH_UNKNOWN)
            {
                calculateSize(buffer, offset + bytesProcessedInThisRun, length);
                if (state != State.LENGTH_KNOWN)
                {
                    unprocessed.putBytes(unprocessedLength, buffer, offset + bytesProcessedInThisRun, length);
                    unprocessedLength += length;
                    return;
                }
            }
            if (state == State.LENGTH_KNOWN)
            {
                int messageLength = bytesBeforeDeclaredLength + declaredLength + CHECKSUM_SIZE;
                if (bytesProcessedInThisRun + messageLength > length + unprocessedLength)
                {
                    // We are (or still are) not able to reach the end of the message
                    int remainingButInsufficient = length - bytesProcessedInThisRun;
                    buffer.getBytes(offset + bytesProcessedInThisRun, unprocessed, unprocessedLength, remainingButInsufficient);
                    unprocessedLength += remainingButInsufficient;
                    return;
                }
                else if (unprocessedLength > 0)
                {
                    // We have enough data when combining unprocessed before and current one
                    int remainingLengthToCompleteMessage = messageLength - unprocessedLength;
                    unprocessed.putBytes(unprocessedLength, buffer, offset + bytesProcessedInThisRun, remainingLengthToCompleteMessage);
                    delineatedDataHandler.onData(unprocessed, 0, messageLength);
                    declaredLength = 0;
                    bytesBeforeDeclaredLength = 0;
                    lengthScanState = LengthScanState.START;
                    state = State.LENGTH_UNKNOWN;
                    unprocessedLength = 0;
                    bytesProcessedInThisRun += remainingLengthToCompleteMessage;
                }
                else
                {
                    // We have enough data in the current buffer
                    delineatedDataHandler.onData(buffer, offset + bytesProcessedInThisRun, messageLength);
                    declaredLength = 0;
                    bytesBeforeDeclaredLength = 0;
                    lengthScanState = LengthScanState.START;
                    state = State.LENGTH_UNKNOWN;
                    unprocessedLength = 0;
                    bytesProcessedInThisRun += messageLength;
                }
            }
        }
        while (bytesProcessedInThisRun < length);
    }

    private void calculateSize(final DirectBuffer buffer, final int offset, final int length)
    {
        for (int c = 0; c < length; c++)
        {
            bytesBeforeDeclaredLength++;
            char ch = (char)buffer.getByte(offset + c);
            if (lengthScanState == LengthScanState.SIZE_VALUE)
            {
                if (ch >= '0' && ch <= '9')
                {
                    declaredLength = declaredLength * 10 + ch - '0';
                }
                else
                {
                    lengthScanState = LengthScanState.START;
                    state = State.LENGTH_KNOWN;
                    return;
                }
            }
            else if (ch == SOH)
            {
                lengthScanState = LengthScanState.SPECIAL;
            }
            else if (lengthScanState == LengthScanState.SPECIAL && ch == '9')
            {
                lengthScanState = LengthScanState.SIZE;
            }
            else if (lengthScanState == LengthScanState.SIZE && ch == '=')
            {
                lengthScanState = LengthScanState.SIZE_VALUE;
            }
            else
            {
                lengthScanState = LengthScanState.NORMAL;
            }
        }
    }

    private enum State
    {
        LENGTH_UNKNOWN,
        LENGTH_KNOWN
    }

    private enum LengthScanState
    {
        START,
        NORMAL,
        SIZE,
        SIZE_VALUE,
        SPECIAL
    }
}

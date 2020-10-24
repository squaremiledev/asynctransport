package dev.squaremile.asynctcpacceptance;

public class SelectiveResponseRequest
{
    private final int respondToEveryNthRequest;

    public SelectiveResponseRequest(final int respondToEveryNthRequest)
    {
        if (respondToEveryNthRequest <= 0)
        {
            throw new IllegalArgumentException("Illegal value " + respondToEveryNthRequest);
        }
        this.respondToEveryNthRequest = respondToEveryNthRequest;
    }

    public boolean shouldRespond(final long requestNumber)
    {
        if (requestNumber < 0)
        {
            throw new IllegalArgumentException("Illegal value " + respondToEveryNthRequest);
        }
        return requestNumber % respondToEveryNthRequest == 0;
    }
}

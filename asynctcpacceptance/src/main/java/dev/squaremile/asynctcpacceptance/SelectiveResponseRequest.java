package dev.squaremile.asynctcpacceptance;

public class SelectiveResponseRequest
{
    private final int totalNumberPfMessagesToSend;
    private final int respondToEveryNthRequest;

    public SelectiveResponseRequest(final int totalNumberPfMessagesToSend, final int respondToEveryNthRequest)
    {
        if (totalNumberPfMessagesToSend <= 0)
        {
            throw new IllegalArgumentException("Illegal value " + totalNumberPfMessagesToSend);
        }
        if (respondToEveryNthRequest <= 0)
        {
            throw new IllegalArgumentException("Illegal value " + respondToEveryNthRequest);
        }
        if (totalNumberPfMessagesToSend % respondToEveryNthRequest != 0)
        {
            throw new IllegalArgumentException("Number of messages to send must be evenly divisible by the response rate");
        }
        this.totalNumberPfMessagesToSend = totalNumberPfMessagesToSend;
        this.respondToEveryNthRequest = respondToEveryNthRequest;
    }

    public boolean shouldRespond(final long requestNumber)
    {
        if (requestNumber < 0)
        {
            throw new IllegalArgumentException("Illegal value " + respondToEveryNthRequest);
        }
        return requestNumber < totalNumberPfMessagesToSend && requestNumber % respondToEveryNthRequest == 0;
    }

    public boolean receivedLast(final long responseNumber)
    {
        if (responseNumber <= 0)
        {
            throw new IllegalArgumentException("Illegal response number " + responseNumber);
        }
        return responseNumber * respondToEveryNthRequest >= totalNumberPfMessagesToSend;
    }
}

package dev.squaremile.trcheck.probe;

class SelectiveResponseRequest
{
    private final int totalNumberOfMessagesToSend;
    private final int respondToEveryNthRequest;

    public SelectiveResponseRequest(final int totalNumberOfMessagesToSend, final int respondToEveryNthRequest)
    {
        if (totalNumberOfMessagesToSend <= 0)
        {
            throw new IllegalArgumentException("Illegal value " + totalNumberOfMessagesToSend);
        }
        if (respondToEveryNthRequest <= 0)
        {
            throw new IllegalArgumentException("Illegal value " + respondToEveryNthRequest);
        }
        if (totalNumberOfMessagesToSend % respondToEveryNthRequest != 0)
        {
            throw new IllegalArgumentException("Number of messages to send must be evenly divisible by the response rate");
        }
        this.totalNumberOfMessagesToSend = totalNumberOfMessagesToSend;
        this.respondToEveryNthRequest = respondToEveryNthRequest;
    }

    public boolean shouldRespond(final long requestNumber)
    {
        if (requestNumber < 0)
        {
            throw new IllegalArgumentException("Illegal value " + respondToEveryNthRequest);
        }
        return requestNumber < totalNumberOfMessagesToSend && requestNumber % respondToEveryNthRequest == 0;
    }

    public boolean receivedLast(final long responseNumber)
    {
        if (responseNumber < 0)
        {
            throw new IllegalArgumentException("Illegal response number " + responseNumber);
        }
        return responseNumber * respondToEveryNthRequest >= totalNumberOfMessagesToSend;
    }
}

package dev.squaremile.transport.usecases.market.domain;

public class Order implements MarketMessage
{
    private long askPrice;
    private int askQuantity;
    private long bidPrice;
    private int bidQuantity;

    public Order(final long bidPrice, final int bidQuantity, final long askPrice, final int askQuantity)
    {
        if (
                (askQuantity != 0 && bidQuantity != 0) ||
                askPrice < 0 || askQuantity < 0 || bidPrice < 0 || bidQuantity < 0
        )
        {
            throw new IllegalArgumentException();
        }
        update(bidPrice, bidQuantity, askPrice, askQuantity);
    }

    public Order(final Order copySource)
    {
        this(copySource.bidPrice, copySource.bidQuantity, copySource.askPrice, copySource.askQuantity);
    }

    public static Order bid(final long bidPrice, final int bidQuantity)
    {
        return new Order(bidPrice, bidQuantity, 0, 0);
    }

    public static Order ask(final long askPrice, final int askQuantity)
    {
        return new Order(0, 0, askPrice, askQuantity);
    }

    public Order withBid(final long bidPrice, final int bidQuantity)
    {
        this.bidPrice = bidPrice;
        this.bidQuantity = bidQuantity;
        this.askPrice = 0;
        this.askQuantity = 0;
        return this;
    }

    public Order withAsk(final long askPrice, final int askQuantity)
    {
        this.askPrice = askPrice;
        this.askQuantity = askQuantity;
        this.bidPrice = 0;
        this.bidQuantity = 0;
        return this;
    }


    public long askPrice()
    {
        return askPrice;
    }

    public int askQuantity()
    {
        return askQuantity;
    }

    public long bidPrice()
    {
        return bidPrice;
    }

    public int bidQuantity()
    {
        return bidQuantity;
    }

    public Side side()
    {
        return bidQuantity > 0 ? Side.BID : Side.ASK;
    }

    public void update(final long bidPrice, final int bidQuantity, final long askPrice, final int askQuantity)
    {
        this.bidPrice = bidPrice;
        this.bidQuantity = bidQuantity;
        this.askPrice = askPrice;
        this.askQuantity = askQuantity;
    }

    @Override
    public String toString()
    {
        return "Order{" +
               "askPrice=" + askPrice +
               ", askQuantity=" + askQuantity +
               ", bidPrice=" + bidPrice +
               ", bidQuantity=" + bidQuantity +
               '}';
    }
}

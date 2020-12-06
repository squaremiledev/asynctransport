package dev.squaremile.transport.usecases.market;

public class Order
{
    private final long askPrice;
    private final int askQuantity;
    private final long bidPrice;
    private final int bidQuantity;

    public Order(final long askPrice, final int askQuantity, final long bidPrice, final int bidQuantity)
    {
        if (
                (askQuantity != 0 && bidQuantity != 0) ||
                askPrice < 0 || askQuantity < 0 || bidPrice < 0 || bidQuantity < 0
        )
        {
            throw new IllegalArgumentException();
        }
        this.askPrice = askPrice;
        this.askQuantity = askQuantity;
        this.bidPrice = bidPrice;
        this.bidQuantity = bidQuantity;
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

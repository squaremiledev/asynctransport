package dev.squaremile.transport.usecases.market;

public class FirmPrice
{
    private long updateTime;
    private long askPrice;
    private int askQuantity;
    private long bidPrice;
    private int bidQuantity;

    public FirmPrice(final int updateTime, final long askPrice, final int askQuantity, final long bidPrice, final int bidQuantity)
    {
        this.updateTime = updateTime;
        this.askPrice = askPrice;
        this.askQuantity = askQuantity;
        this.bidPrice = bidPrice;
        this.bidQuantity = bidQuantity;
    }

    public static FirmPrice createNoPrice()
    {
        return new FirmPrice(0, 0, 0, 0, 0);
    }

    public void update(final long currentTime, final FirmPrice source)
    {
        this.askPrice = source.askPrice;
        this.askQuantity = source.askQuantity;
        this.bidPrice = source.bidPrice;
        this.bidQuantity = source.bidQuantity;
        this.updateTime = currentTime;
    }

    public boolean execute(final long currentTime, final Order order)
    {
        if (
                (order.bidQuantity() > 0 && order.askQuantity() > 0) ||
                order.askPrice() != askPrice ||
                order.bidPrice() != bidPrice ||
                order.askQuantity() < 0 ||
                order.bidQuantity() < 0 ||
                order.askQuantity() > askQuantity ||
                order.bidQuantity() > bidQuantity)
        {
            return false;
        }
        this.askQuantity -= order.askQuantity();
        this.bidQuantity -= order.bidQuantity();
        this.updateTime = currentTime;
        return true;
    }

    public long updateTime()
    {
        return updateTime;
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
        return "FirmPrice{" +
               "updateTime=" + updateTime +
               ", askPrice=" + askPrice +
               ", askQuantity=" + askQuantity +
               ", bidPrice=" + bidPrice +
               ", bidQuantity=" + bidQuantity +
               '}';
    }
}

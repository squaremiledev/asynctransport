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

    public boolean execute(final long currentTime, final FirmPrice executedQuantity)
    {
        if (
                (executedQuantity.bidQuantity > 0 && executedQuantity.askQuantity > 0) ||
                executedQuantity.askPrice != askPrice ||
                executedQuantity.bidPrice != bidPrice ||
                executedQuantity.askQuantity < 0 ||
                executedQuantity.bidQuantity < 0 ||
                executedQuantity.askQuantity > askQuantity ||
                executedQuantity.bidQuantity > bidQuantity)
        {
            return false;
        }
        this.askQuantity -= executedQuantity.askQuantity;
        this.bidQuantity -= executedQuantity.bidQuantity;
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

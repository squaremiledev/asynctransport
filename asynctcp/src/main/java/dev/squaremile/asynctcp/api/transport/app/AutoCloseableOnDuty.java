package dev.squaremile.asynctcp.api.transport.app;

public interface AutoCloseableOnDuty extends OnDuty, AutoCloseable
{
    AutoCloseableOnDuty NO_OP = new AutoCloseableOnDuty()
    {
        @Override
        public void close()
        {

        }

        @Override
        public void work()
        {

        }
    };

    @Override
    void close();
}

package dev.squaremile.asynctcpacceptance.demo;

public class ApplicationLifecycle implements SingleLocalConnectionDemoApplication.LifecycleListener
{
    private boolean isUp = false;

    @Override
    public void onUp()
    {
        isUp = true;
    }

    @Override
    public void onDown()
    {
        isUp = false;
    }

    public boolean isUp()
    {
        return isUp;
    }
}

package dev.squaremile.asynctcpacceptance.sampleapps;

class ApplicationLifecycle implements SingleLocalConnectionApplication.LifecycleListener
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

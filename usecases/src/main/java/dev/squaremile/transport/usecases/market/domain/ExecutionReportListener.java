package dev.squaremile.transport.usecases.market.domain;

public interface ExecutionReportListener
{
    void onExecution(final ExecutionReport executionReport);
}

package dev.squaremile.transport.usecases.market.domain;

public interface ExecutionReportListener
{
    ExecutionReportListener IGNORE = executionReport ->
    {
    };

    void onExecution(final ExecutionReport executionReport);
}

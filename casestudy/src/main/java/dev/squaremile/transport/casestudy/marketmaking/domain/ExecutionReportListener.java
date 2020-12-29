package dev.squaremile.transport.casestudy.marketmaking.domain;

public interface ExecutionReportListener
{
    ExecutionReportListener IGNORE = executionReport ->
    {
    };

    void onExecution(final ExecutionReport executionReport);
}

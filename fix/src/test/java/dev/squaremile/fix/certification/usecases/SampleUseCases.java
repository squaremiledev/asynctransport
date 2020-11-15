package dev.squaremile.fix.certification.usecases;

import dev.squaremile.fix.certification.UseCase;

public enum SampleUseCases implements UseCase
{
    USE_CASE_001_ACCEPTED_LOGON("FIX.4.2", "useCase001"),
    USE_CASE_002_REJECTED_LOGON("FIXT.1.1", "useCase002"),
    USE_CASE_002_NOT_RESPONDING("FIXT.1.1", "useCase003");

    private final String fixVersion;
    private final String useCaseAsUsername;

    SampleUseCases(final String fixVersion, final String useCaseAsUsername)
    {
        this.fixVersion = fixVersion;
        this.useCaseAsUsername = useCaseAsUsername;
    }


    @Override
    public String fixVersion()
    {
        return fixVersion;
    }

    @Override
    public String username()
    {
        return useCaseAsUsername;
    }
}

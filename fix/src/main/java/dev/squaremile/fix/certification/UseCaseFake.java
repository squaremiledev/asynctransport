package dev.squaremile.fix.certification;

public class UseCaseFake implements UseCase
{
    private final UseCase useCase;
    private final FakeApplicationFactory applicationFactory;

    public UseCaseFake(final UseCase useCase, final FakeApplicationFactory applicationFactory)
    {
        this.useCase = useCase;
        this.applicationFactory = applicationFactory;
    }


    @Override
    public String fixVersion()
    {
        return useCase.fixVersion();
    }

    @Override
    public String username()
    {
        return useCase.username();
    }

    public FakeApplicationFactory applicationFactory()
    {
        return applicationFactory;
    }
}

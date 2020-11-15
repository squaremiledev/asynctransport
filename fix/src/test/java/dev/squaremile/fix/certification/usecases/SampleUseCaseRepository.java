package dev.squaremile.fix.certification.usecases;

import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;
import dev.squaremile.fix.certification.FakeApplication;
import dev.squaremile.fix.certification.FakeApplicationRepository;
import dev.squaremile.fix.certification.IgnoreAll;
import dev.squaremile.fix.certification.UseCaseFake;
import dev.squaremile.fix.certification.UseCasesRepository;

public class SampleUseCaseRepository implements FakeApplicationRepository
{
    private final UseCasesRepository repository;

    public SampleUseCaseRepository()
    {
        repository = new UseCasesRepository(
                new UseCaseFake(SampleUseCases.USE_CASE_001_ACCEPTED_LOGON, (connectionTransport, connectionId) -> new RespondToLogOnIgnoreRest(connectionTransport)),
                new UseCaseFake(SampleUseCases.USE_CASE_002_REJECTED_LOGON, (connectionTransport, connectionId) -> new RejectLogOnIgnoreRest(connectionTransport)),
                new UseCaseFake(SampleUseCases.USE_CASE_002_NOT_RESPONDING, (connectionTransport, connectionId) -> new IgnoreAll())
        );
    }

    @Override
    public FakeApplication create(
            final ConnectionTransport connectionTransport, final ConnectionId connectionId, final String fixVersion, final String username
    )
    {
        return repository.create(connectionTransport, connectionId, fixVersion, username);
    }
}

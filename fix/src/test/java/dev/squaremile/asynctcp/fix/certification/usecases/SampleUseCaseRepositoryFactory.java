package dev.squaremile.asynctcp.fix.certification.usecases;

import dev.squaremile.asynctcp.certification.UseCaseApplicationFactory;
import dev.squaremile.asynctcp.fix.FixMetadata;
import dev.squaremile.asynctcp.fix.FixUCFake;
import dev.squaremile.asynctcp.fix.FixUCRepository;
import dev.squaremile.asynctcp.fix.IgnoreAll;
import dev.squaremile.asynctcp.transport.api.app.ConnectionApplication;
import dev.squaremile.asynctcp.transport.api.app.ConnectionTransport;
import dev.squaremile.asynctcp.transport.api.values.ConnectionId;

public class SampleUseCaseRepositoryFactory implements UseCaseApplicationFactory<FixMetadata>
{
    private final FixUCRepository repository;

    public SampleUseCaseRepositoryFactory()
    {
        repository = new FixUCRepository(
                new FixUCFake(SampleUseCases.USE_CASE_001_ACCEPTED_LOGON, (connectionTransport, connectionId) -> new RespondToLogOnIgnoreRest(connectionTransport)),
                new FixUCFake(SampleUseCases.USE_CASE_002_REJECTED_LOGON, (connectionTransport, connectionId) -> new RejectLogOnIgnoreRest(connectionTransport)),
                new FixUCFake(SampleUseCases.USE_CASE_002_NOT_RESPONDING, (connectionTransport, connectionId) -> new IgnoreAll())
        );
    }

    @Override
    public ConnectionApplication create(
            final ConnectionTransport connectionTransport, final ConnectionId connectionId, final FixMetadata metadata
    )
    {
        return repository.create(connectionTransport, connectionId, metadata);
    }
}

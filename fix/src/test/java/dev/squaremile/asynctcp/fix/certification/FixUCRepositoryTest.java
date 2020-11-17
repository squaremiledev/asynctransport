package dev.squaremile.asynctcp.fix.certification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;


import dev.squaremile.asynctcp.fix.FixUCFake;
import dev.squaremile.asynctcp.fix.FixUCRepository;
import dev.squaremile.asynctcp.fix.IgnoreAll;
import dev.squaremile.asynctcp.fix.certification.usecases.SampleUseCases;

class FixUCRepositoryTest
{
    @Test
    void shouldRejectClashingUseCases()
    {
        assertThrows(IllegalArgumentException.class, () -> new FixUCRepository(
                new FixUCFake(SampleUseCases.USE_CASE_001_ACCEPTED_LOGON, (connectionTransport, connectionId) -> new IgnoreAll()),
                new FixUCFake(SampleUseCases.USE_CASE_001_ACCEPTED_LOGON, (connectionTransport, connectionId) -> new IgnoreAll())
        ));
    }
}
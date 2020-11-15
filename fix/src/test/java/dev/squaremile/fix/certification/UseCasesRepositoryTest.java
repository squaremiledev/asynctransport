package dev.squaremile.fix.certification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;


import dev.squaremile.fix.certification.usecases.SampleUseCases;

class UseCasesRepositoryTest
{
    @Test
    void shouldRejectClashingUseCases()
    {
        assertThrows(IllegalArgumentException.class, () -> new UseCasesRepository(
                new UseCaseFake(SampleUseCases.USE_CASE_001_ACCEPTED_LOGON, (connectionTransport, connectionId) -> new IgnoreAll()),
                new UseCaseFake(SampleUseCases.USE_CASE_001_ACCEPTED_LOGON, (connectionTransport, connectionId) -> new IgnoreAll())
        ));
    }
}
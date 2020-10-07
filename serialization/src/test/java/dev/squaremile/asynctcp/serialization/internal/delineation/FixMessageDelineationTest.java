package dev.squaremile.asynctcp.serialization.internal.delineation;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static dev.squaremile.asynctcp.serialization.internal.delineation.DataFixtures.bufferWith;
import static dev.squaremile.asynctcp.serialization.internal.delineation.DataFixtures.bytes;

class FixMessageDelineationTest
{
    private final DelineatedDataSpy delineatedDataSpy = new DelineatedDataSpy();
    private final FixMessageDelineation delineation = new FixMessageDelineation(delineatedDataSpy);

    private static byte[] asciiFix(final String content)
    {
        return content.replaceAll("\\^", "\u0001").getBytes(StandardCharsets.US_ASCII);
    }

    private static String str(final byte[] content)
    {
        return new String(content, StandardCharsets.US_ASCII);
    }

    @Test
    void shouldIgnoreEmptyData()
    {
        delineation.onData(bufferWith(new byte[]{}), 0, 0);
        delineation.onData(bufferWith(new byte[]{1}), 1, 0);

        assertThat(notified()).isEmpty();
    }

    @Test
    void shouldDetectFixMessage()
    {
        delineation.onData(bufferWith(bytes(new byte[3], completeFixMessage(), new byte[2])), 3, completeFixMessage().length);

        assertThat(notified()).containsExactly(str(completeFixMessage()));
    }

    @Test
    void shouldDetectDelineationPointBetweenMessages()
    {
        byte[] content1 = asciiFix("8=FIX.4.0^9=58^35=0^49=BuySide^56=SellSide^34=5^52=20190605-11:57:29.363^10=175^");
        byte[] content2 = asciiFix("8=FIX.4.0^9=55^35=0^49=BuyS^56=SellSide^34=5^52=20190605-11:57:29.363^10=175^");

        delineation.onData(bufferWith(bytes(content1, content2)), 0, content1.length + content2.length);

        assertThat(notified()).containsExactly(str(content1), str(content2));
    }

    @Test
    void shouldDelineateAndReassembleMultipleMessages()
    {
        byte[] content1 = asciiFix("8=FIX.4.0^9=58^35=0^49=BuySide^56=SellSide^34=5^52=20190605-11:57:29.363^10=175^8=FIX.4.0^9=55^35=0^49=BuyS^5");
        byte[] content2 = asciiFix("6=SellSide^34=5^52=20190605-11:57:29.363^10=175^8=FIX.4.0^9");
        byte[] content3 = asciiFix("=50^35=0^49=BuyS^56=Sel^34=5^52=20190605-11:57:29.363^10=175^");

        delineation.onData(bufferWith(bytes(content1, content2, content3)), 0, content1.length + content2.length + content3.length);

        assertThat(notified()).containsExactly(
                str(asciiFix("8=FIX.4.0^9=58^35=0^49=BuySide^56=SellSide^34=5^52=20190605-11:57:29.363^10=175^")),
                str(asciiFix("8=FIX.4.0^9=55^35=0^49=BuyS^56=SellSide^34=5^52=20190605-11:57:29.363^10=175^")),
                str(asciiFix("8=FIX.4.0^9=50^35=0^49=BuyS^56=Sel^34=5^52=20190605-11:57:29.363^10=175^"))
        );
    }

    @Test
    void shouldNotNotifyAboutIncompleteMessage()
    {
        delineation.onData(bufferWith(completeFixMessage()), 0, completeFixMessage().length - 1);

        assertThat(notified()).isEmpty();
    }

    @Test
    void shouldHandleMessagesArrivingInTwoChunks()
    {
        delineation.onData(bufferWith(completeFixMessage()), 0, completeFixMessage().length - 1);
        assertThat(notified()).isEmpty();

        delineation.onData(bufferWith(completeFixMessage()), completeFixMessage().length - 1, 1);
        assertThat(notified()).containsExactly(str(completeFixMessage()));
    }

    @Test
    void shouldHandleMessagesArrivingInMultipleChunks()
    {
        byte[] partA = asciiFix(" 8=FIX.4.0^9=58^35=0^49=BuySide^56=SellSide^34=5^52=20190605-11:57:29.3  ");
        byte[] partB = asciiFix("  63^1 ");
        byte[] partC = asciiFix("   0=175^ ");
        delineation.onData(bufferWith(partA), 1, partA.length - 3);
        assertThat(notified()).isEmpty();

        delineation.onData(bufferWith(partB), 2, 4);
        assertThat(notified()).isEmpty();

        delineation.onData(bufferWith(partC), 3, 6);
        assertThat(notified()).containsExactly(str(asciiFix("8=FIX.4.0^9=58^35=0^49=BuySide^56=SellSide^34=5^52=20190605-11:57:29.363^10=175^")));
    }

    @Test
    void shouldReAssembleMessagesFromIndividualBytes()
    {
        byte[] fullMessage = completeFixMessage();

        for (int i = 0; i < fullMessage.length; i++)
        {
            delineation.onData(bufferWith(fullMessage), i, 1);
        }

        assertThat(notified()).containsExactly(str(fullMessage));
    }

    @Test
    void shouldOnlyConsiderLengthFieldWhenCalculatingLength()
    {
        // technically not correct fix, as the length must be the second field, but useful for state machine validation
        byte[] message = asciiFix("8=FIX.4.0^99=28^9=58^35=0^49=BuySide^56=SellSide^34=5^52=20190605-11:57:29.363^10=175^");

        delineation.onData(bufferWith(message), 0, message.length);

        assertThat(notified()).containsExactly(str(asciiFix("8=FIX.4.0^99=28^9=58^35=0^49=BuySide^56=SellSide^34=5^52=20190605-11:57:29.363^10=175^")));
    }

    private List<String> notified()
    {
        return delineatedDataSpy.received().stream().map(FixMessageDelineationTest::str).collect(Collectors.toList());
    }

    private byte[] completeFixMessage()
    {
        return asciiFix("8=FIX.4.0^9=58^35=0^49=BuySide^56=SellSide^34=5^52=20190605-11:57:29.363^10=175^");
    }
}
package com.michaelszymczak.sample.sockets;

import com.michaelszymczak.sample.sockets.support.AcceptingServer;
import com.michaelszymczak.sample.sockets.support.SampleClient;
import com.michaelszymczak.sample.sockets.support.ServerRun;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


import static com.michaelszymczak.sample.sockets.support.ServerRun.startServer;
import static com.michaelszymczak.sample.sockets.support.StringFixtures.byteArrayWith;
import static com.michaelszymczak.sample.sockets.support.StringFixtures.stringWith;

class SampleClientTest
{

    /*
Wireshark's capturing filter: tcp.port == 4847
(used AcceptingServer.returningUponConnection 4847)

113	1.612664105	127.0.0.1	127.0.0.1	TCP	74	39992 → 4847 [SYN] Seq=0 Win=65495 Len=0 MSS=65495 SACK_PERM=1 TSval=71669757 TSecr=0 WS=128
114	1.612677341	127.0.0.1	127.0.0.1	TCP	74	4847 → 39992 [SYN, ACK] Seq=0 Ack=1 Win=65483 Len=0 MSS=65495 SACK_PERM=1 TSval=71669757 TSecr=71669757 WS=128
115	1.612687874	127.0.0.1	127.0.0.1	TCP	66	39992 → 4847 [ACK] Seq=1 Ack=1 Win=65536 Len=0 TSval=71669757 TSecr=71669757
116	1.614684213	127.0.0.1	127.0.0.1	TCP	73	4847 → 39992 [PSH, ACK] Seq=1 Ack=1 Win=65536 Len=7 TSval=71669759 TSecr=71669757
117	1.614696346	127.0.0.1	127.0.0.1	TCP	66	39992 → 4847 [ACK] Seq=1 Ack=8 Win=65536 Len=0 TSval=71669759 TSecr=71669759
118	1.618240169	127.0.0.1	127.0.0.1	TCP	66	39992 → 4847 [FIN, ACK] Seq=1 Ack=8 Win=65536 Len=0 TSval=71669763 TSecr=71669759
119	1.620836053	127.0.0.1	127.0.0.1	TCP	66	4847 → 39992 [ACK] Seq=8 Ack=2 Win=65536 Len=0 TSval=71669766 TSecr=71669763
127	1.945067547	127.0.0.1	127.0.0.1	TCP	66	4847 → 39992 [FIN, ACK] Seq=8 Ack=2 Win=65536 Len=0 TSval=71670090 TSecr=71669763
128	1.945080665	127.0.0.1	127.0.0.1	TCP	66	39992 → 4847 [ACK] Seq=2 Ack=9 Win=65536 Len=0 TSval=71670090 TSecr=71670090
     */

    @Test
    void shouldReadContent() throws Exception
    {
        // Given
        try (
                ServerRun serverRun = startServer(AcceptingServer.returningUponConnection(0, byteArrayWith("hello!\n")));
                SampleClient client = new SampleClient()
        )
        {
            // When
            final byte[] actualReadContent = client.connectedTo(serverRun.serverPort())
                    .read(byteArrayWith("hello!\n").length, 10);

            // Then
            assertEquals(stringWith(byteArrayWith("hello!\n"), 10), stringWith(actualReadContent));
        }
    }

}

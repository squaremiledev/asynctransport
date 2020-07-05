package com.michaelszymczak.sample.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.michaelszymczak.sample.sockets.support.AcceptingServer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


import static java.nio.charset.StandardCharsets.US_ASCII;

class BootstrapTest
{

    /*
55	1.599395701	127.0.0.1	127.0.0.1	TCP	74	44424 → 2023 [SYN] Seq=0 Win=65495 Len=0 MSS=65495 SACK_PERM=1 TSval=66843834 TSecr=0 WS=128
57	1.599404282	127.0.0.1	127.0.0.1	TCP	74	2023 → 44424 [SYN, ACK] Seq=0 Ack=1 Win=65483 Len=0 MSS=65495 SACK_PERM=1 TSval=66843834 TSecr=66843834 WS=128
58	1.599412313	127.0.0.1	127.0.0.1	TCP	66	44424 → 2023 [ACK] Seq=1 Ack=1 Win=65536 Len=0 TSval=66843834 TSecr=66843834
59	1.800844805	127.0.0.1	127.0.0.1	TCP	73	2023 → 44424 [PSH, ACK] Seq=1 Ack=1 Win=65536 Len=7 TSval=66844035 TSecr=66843834
60	1.800864962	127.0.0.1	127.0.0.1	TCP	66	44424 → 2023 [ACK] Seq=1 Ack=8 Win=65536 Len=0 TSval=66844035 TSecr=66844035
63	2.131673503	127.0.0.1	127.0.0.1	TCP	66	2023 → 44424 [FIN, ACK] Seq=8 Ack=1 Win=65536 Len=0 TSval=66844366 TSecr=66844035
64	2.131692874	127.0.0.1	127.0.0.1	TCP	66	44424 → 2023 [FIN, ACK] Seq=1 Ack=9 Win=65536 Len=0 TSval=66844366 TSecr=66844366
65	2.131700092	127.0.0.1	127.0.0.1	TCP	66	2023 → 44424 [ACK] Seq=9 Ack=2 Win=65536 Len=0 TSval=66844366 TSecr=66844366
     */

    private static final int ONE_SECOND_IN_MS = (int)TimeUnit.SECONDS.toMillis(1);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Test
    void bootstrap() throws IOException, InterruptedException, ExecutionException
    {
        // Given
        final AcceptingServer server = new AcceptingServer(0);
        Future<?> serverTask = startServer(server);

        // When
        Socket socket = new Socket();
        final byte[] expectedContent = "hello!\n".getBytes(US_ASCII);
        byte[] expectedReceivedContent = Arrays.copyOf(expectedContent, 10);
        byte[] receivedContent = new byte[expectedReceivedContent.length];
        socket.connect(new InetSocketAddress("127.0.0.1", server.port()), ONE_SECOND_IN_MS);
        socket.setSoTimeout(ONE_SECOND_IN_MS * 2);
        int bytesRead = socket.getInputStream().read(receivedContent);

        // Then
        assertEquals(new String(expectedReceivedContent, US_ASCII), new String(receivedContent, US_ASCII));
        assertEquals(expectedContent.length, bytesRead);
        serverTask.cancel(true);
    }

    private Future<?> startServer(final AcceptingServer acceptingServer) throws InterruptedException, ExecutionException
    {
        Future<?> serverTask = executorService.submit(acceptingServer::startServer);
        acceptingServer.waitUntilReady();
        if (serverTask.isDone())
        {
            serverTask.get();
        }
        return serverTask;
    }

}

package com.michaelszymczak.sample.sockets;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BootstrapTest {

    private static final int ONE_SECOND_IN_MS = (int) TimeUnit.SECONDS.toMillis(1);

    @Test
    @Disabled
    void bootstrap() throws IOException {
        /*
        sudo apt install ucspi-tcp
        tcpserver -v -RHl0 127.0.0.1 2023 sh -c "sleep 1 && echo 'hello!'"
        tcpserver: status: 0/40
         */
        Socket socket = new Socket();
        byte[] expectedReceivedContent = "hello!\n".getBytes(US_ASCII);
        byte[] receivedContent = new byte[expectedReceivedContent.length];
        socket.connect(new InetSocketAddress("127.0.0.1", 2023), ONE_SECOND_IN_MS);
        socket.setSoTimeout(ONE_SECOND_IN_MS * 2);
        int bytesRead = socket.getInputStream().read(receivedContent);

        assertEquals(expectedReceivedContent.length, bytesRead);
        assertEquals(new String(expectedReceivedContent, US_ASCII), new String(receivedContent, US_ASCII));
        /*
        tcpserver: status: 1/40
        tcpserver: pid 12385 from 127.0.0.1
        tcpserver: ok 12385 0:127.0.0.1:2023 :127.0.0.1::43666
        tcpserver: end 12385 status 0
        tcpserver: status: 0/40
        ^C
         */
    }
}

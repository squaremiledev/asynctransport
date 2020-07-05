package com.michaelszymczak.sample.sockets;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

class BootstrapTest {

    @Test
    @Disabled
    void bootstrap() throws IOException {
        /*
        sudo apt install ucspi-tcp
        tcpserver -v -RHl0 127.0.0.1 2023 sh -c "echo 'hello!'"
        tcpserver: status: 0/40
         */
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 2023), (int) TimeUnit.SECONDS.toMillis(1));
        /*
        tcpserver: status: 1/40
        tcpserver: pid 9848 from 127.0.0.1
        tcpserver: ok 9848 0:127.0.0.1:2023 :127.0.0.1::43424
        tcpserver: end 9848 status 0
        tcpserver: status: 0/40
        ^C
         */
    }
}

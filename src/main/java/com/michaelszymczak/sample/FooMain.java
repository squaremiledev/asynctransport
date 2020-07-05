package com.michaelszymczak.sample;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class FooMain {

    public static void main(String[] args) throws IOException {
        System.out.println(new FooMain().foo());
        byte[] arr = new byte[1000];
        ByteBuffer byteBuffer = ByteBuffer.wrap(arr);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        Selector serverSocketSelector = Selector.open();
        serverSocketChannel.register(serverSocketSelector, serverSocketChannel.validOps());
        serverSocketChannel.bind(new InetSocketAddress(2023));
        System.out.println("serverSocketChannel.socket().getReceiveBufferSize() = " + serverSocketChannel.socket().getReceiveBufferSize());
        while (true)
        {
            int selectedKeys = serverSocketSelector.selectNow();
            if (selectedKeys > 0) {
                for (SelectionKey selectedKey : serverSocketSelector.selectedKeys()) {
                    if (!selectedKey.isValid())
                    {
                        continue;
                    }
                    if (selectedKey.isAcceptable())
                    {
                        System.out.println("ACCEPTABLE selectedKey = " + selectedKey);
                    }
                    else
                    {
                        throw new IllegalStateException(selectedKey.toString());
                    }

                }
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
//        while (true) {
//            SocketChannel accept = serverSocketChannel.accept();
//            System.out.println("accept.socket().getReceiveBufferSize() = " + accept.socket().getReceiveBufferSize());
//            System.out.println("accept.socket().getSendBufferSize() = " + accept.socket().getSendBufferSize());
//            System.out.println("accepted");
//            int read = accept.read(byteBuffer);
//            System.out.println("read " + read + ": " + Arrays.toString(byteBuffer.array()));
//            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
//        }
    }

    public String foo() {
        return "bar";
    }

}

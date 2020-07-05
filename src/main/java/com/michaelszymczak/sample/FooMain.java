package com.michaelszymczak.sample;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

public class FooMain
{

    public static void main(String[] args) throws IOException
    {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
//        Selector selector = Selector.open();
//        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
//        serverSocketChannel.bind(new InetSocketAddress(2023));
//        System.out.println("serverSocketChannel.socket().getReceiveBufferSize() = " + serverSocketChannel.socket().getReceiveBufferSize());
//        while (true) {
//            int selectedKeys = selector.selectNow();
//            if (selectedKeys > 0) {
//                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
//                while (selectionKeyIterator.hasNext())
//                {
//                    SelectionKey selectedKey = selectionKeyIterator.next();
//                    selectionKeyIterator.remove();
//                    if (!selectedKey.isValid()) {
//                        continue;
//                    }
//                    if (selectedKey.isAcceptable()) {
//                        System.out.println("ACCEPTABLE selectedKey = " + selectedKey);
//                        ServerSocketChannel channel = (ServerSocketChannel) selectedKey.channel();
//                        SocketChannel socketChannel = channel.accept();
//                        if (socketChannel != null) {
//                            socketChannel.configureBlocking(false);
//                            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_CONNECT);
//                        }
//                    } else if (selectedKey.isConnectable()) {
//                        System.out.println("CONNECT");
//                    } else if (selectedKey.isReadable()) {
//                        System.out.println("READ");
//                        SocketChannel channel = (SocketChannel)selectedKey.channel();
//                        int read = channel.read(ByteBuffer.allocate(10));
//                        System.out.println("read = " + read);
//                    } else if (selectedKey.isWritable()) {
//                        System.out.println("WRITE");
//                    } else {
//                        System.out.println("SOMETHING_ELSE?");
//                    }
//
//                }
//            }
//            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
//        }
    }

    public String foo()
    {
        return "bar";
    }

}

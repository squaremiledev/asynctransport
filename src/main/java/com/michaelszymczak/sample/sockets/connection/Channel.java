package com.michaelszymczak.sample.sockets.connection;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Channel extends AutoCloseable
{
    int write(ByteBuffer src) throws IOException;

    int read(ByteBuffer dst) throws IOException;
}

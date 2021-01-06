package dev.squaremile.asynctcp.internal.transport.domain.connection;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface Channel extends WritableByteChannel, ReadableByteChannel
{

}

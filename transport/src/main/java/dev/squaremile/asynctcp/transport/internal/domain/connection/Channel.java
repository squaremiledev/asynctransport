package dev.squaremile.asynctcp.transport.internal.domain.connection;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface Channel extends WritableByteChannel, ReadableByteChannel
{

}

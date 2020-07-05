package com.michaelszymczak.sample.sockets.support;

import java.util.Arrays;


import static java.nio.charset.StandardCharsets.US_ASCII;

public class StringFixtures
{
    public static String stringWith(final byte[] content)
    {
        return new String(content, US_ASCII);
    }

    public static String stringWith(final String content, final int length)
    {
        return stringWith(byteArrayWith(content, length));
    }

    public static String stringWith(final byte[] content, final int length)
    {
        return stringWith(Arrays.copyOf(content, length));
    }

    public static byte[] byteArrayWith(final String content, final int arrayLength)
    {
        return Arrays.copyOf(byteArrayWith(content), arrayLength);
    }

    public static byte[] byteArrayWith(final String content)
    {
        return content.getBytes(US_ASCII);
    }
}

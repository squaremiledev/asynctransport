package dev.squaremile.asynctcp.support;

import java.util.Arrays;
import java.util.function.IntFunction;


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

    public static byte[] byteArrayWith(final IntFunction<String> content, final int itemsGenerated)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < itemsGenerated; i++)
        {
            sb.append(content.apply(i));
        }

        return byteArrayWith(sb.toString());
    }

    public static byte[] byteArrayWith(final String content)
    {
        return content.getBytes(US_ASCII);
    }

    public static String fixedLengthStringStartingWith(final String content, final int minLength)
    {
        final StringBuilder sb = new StringBuilder(10);
        sb.append(content);
        for (int i = 0; i < minLength - content.length(); i++)
        {
            sb.append(i % 10);
        }
        return sb.toString();
    }
}

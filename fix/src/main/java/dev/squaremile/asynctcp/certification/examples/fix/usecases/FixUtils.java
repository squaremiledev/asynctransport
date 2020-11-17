package dev.squaremile.asynctcp.certification.examples.fix.usecases;

import java.nio.charset.StandardCharsets;

public class FixUtils
{
    public static byte[] asciiFixBody(final String fixVersion, final String content)
    {
        return asciiFix("8=" + fixVersion + "^9=" + content.length() + "^" + content + "10=079^");
    }

    public static boolean isLogon(final CharSequence content, final int startPosition)
    {
        return content.charAt(startPosition) == '\u0001' &&
               content.charAt(startPosition + 1) == '3' &&
               content.charAt(startPosition + 2) == '5' &&
               content.charAt(startPosition + 3) == '=' &&
               content.charAt(startPosition + 4) == 'A' &&
               content.charAt(startPosition + 5) == '\u0001';
    }

    private static byte[] asciiFix(final String content)
    {
        return content.replaceAll("\\^", "\u0001").getBytes(StandardCharsets.US_ASCII);
    }
}

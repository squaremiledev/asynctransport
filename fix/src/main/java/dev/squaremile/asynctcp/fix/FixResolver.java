package dev.squaremile.asynctcp.fix;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agrona.AsciiSequenceView;


import dev.squaremile.asynctcp.certification.Resolver;
import dev.squaremile.asynctcp.transport.api.events.MessageReceived;

class FixResolver implements Resolver<FixMetadata>
{
    private final AsciiSequenceView content = new AsciiSequenceView();
    private final Pattern fixVersionPattern = Pattern.compile("8=(.*?)\u0001");
    private final Pattern usernamePattern = Pattern.compile("\u0001553=(.*?)\u0001");

    @Override
    public Optional<FixMetadata> useCase(final MessageReceived messageReceived)
    {
        final String fixMessage = fixMessage(messageReceived);
        if (fixMessage.contains("\u000135=A\u0001"))
        {
            return Optional.of(new FixMetadata(username(fixMessage), fixVersion(fixMessage)));
        }
        return Optional.empty();
    }

    private String fixMessage(final MessageReceived message)
    {
        content.wrap(message.buffer(), message.offset(), message.length());
        return content.toString();
    }

    private String username(final String fixMessage)
    {
        return matchedValue(usernamePattern.matcher(fixMessage));
    }

    private String matchedValue(final Matcher matcher)
    {
        return (matcher.find()) ? matcher.group(1) : "";
    }

    private String fixVersion(final String fixMessage)
    {
        return matchedValue(fixVersionPattern.matcher(fixMessage));
    }
}

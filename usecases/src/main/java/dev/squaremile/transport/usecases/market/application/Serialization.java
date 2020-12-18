package dev.squaremile.transport.usecases.market.application;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;


import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;
import dev.squaremile.transport.usecases.market.schema.FirmPriceDecoder;
import dev.squaremile.transport.usecases.market.schema.FirmPriceEncoder;
import dev.squaremile.transport.usecases.market.schema.MessageHeaderDecoder;
import dev.squaremile.transport.usecases.market.schema.MessageHeaderEncoder;

public class Serialization
{
    private final FirmPrice decodedFirmPrice = FirmPrice.createNoPrice();
    private final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
    private final MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
    private final FirmPriceEncoder firmPriceUpdateEncoder = new FirmPriceEncoder();
    private final FirmPriceDecoder firmPriceUpdateDecoder = new FirmPriceDecoder();

    public int encode(final FirmPrice firmPrice, final MutableDirectBuffer buffer, final int offset)
    {
        firmPriceUpdateEncoder.wrapAndApplyHeader(buffer, offset, messageHeaderEncoder)
                .correlationId(firmPrice.correlationId())
                .updateTime(firmPrice.updateTime())
                .bidPrice(firmPrice.bidPrice())
                .bidQuantity(firmPrice.bidQuantity())
                .askPrice(firmPrice.askPrice())
                .askQuantity(firmPrice.askQuantity());
        return messageHeaderEncoder.encodedLength() + firmPriceUpdateEncoder.encodedLength();
    }

    public MarketMessage decode(final DirectBuffer buffer, final int offset)
    {
        messageHeaderDecoder.wrap(buffer, offset);
        if (messageHeaderDecoder.templateId() == FirmPriceDecoder.TEMPLATE_ID)
        {
            int bodyOffset = offset + messageHeaderDecoder.encodedLength();
            firmPriceUpdateDecoder.wrap(
                    buffer,
                    bodyOffset,
                    messageHeaderDecoder.blockLength(),
                    messageHeaderDecoder.version()
            );
            decodedFirmPrice.update(
                    firmPriceUpdateDecoder.correlationId(),
                    firmPriceUpdateDecoder.updateTime(),
                    firmPriceUpdateDecoder.bidPrice(),
                    firmPriceUpdateDecoder.bidQuantity(),
                    firmPriceUpdateDecoder.askPrice(),
                    firmPriceUpdateDecoder.askQuantity()
            );
            return decodedFirmPrice;
        }
        return null;
    }
}

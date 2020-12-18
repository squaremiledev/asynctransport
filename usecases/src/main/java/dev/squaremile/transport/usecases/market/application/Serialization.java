package dev.squaremile.transport.usecases.market.application;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;


import dev.squaremile.transport.usecases.market.domain.FirmPrice;
import dev.squaremile.transport.usecases.market.domain.MarketMessage;
import dev.squaremile.transport.usecases.market.domain.Order;
import dev.squaremile.transport.usecases.market.domain.OrderResult;
import dev.squaremile.transport.usecases.market.schema.ExecutionResult;
import dev.squaremile.transport.usecases.market.schema.FirmPriceDecoder;
import dev.squaremile.transport.usecases.market.schema.FirmPriceEncoder;
import dev.squaremile.transport.usecases.market.schema.MessageHeaderDecoder;
import dev.squaremile.transport.usecases.market.schema.MessageHeaderEncoder;
import dev.squaremile.transport.usecases.market.schema.OrderDecoder;
import dev.squaremile.transport.usecases.market.schema.OrderEncoder;
import dev.squaremile.transport.usecases.market.schema.OrderResultDecoder;
import dev.squaremile.transport.usecases.market.schema.OrderResultEncoder;

public class Serialization
{
    private final FirmPrice decodedFirmPrice = FirmPrice.createNoPrice();
    private final Order decodedOrder = new Order(0, 0, 0, 0);
    private final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
    private final MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
    private final FirmPriceEncoder firmPriceEncoder = new FirmPriceEncoder();
    private final FirmPriceDecoder firmPriceDecoder = new FirmPriceDecoder();
    private final OrderEncoder orderEncoder = new OrderEncoder();
    private final OrderDecoder orderDecoder = new OrderDecoder();
    private final OrderResultEncoder orderResultEncoder = new OrderResultEncoder();
    private final OrderResultDecoder orderResultDecoder = new OrderResultDecoder();

    private static ExecutionResult toExecutionResult(final OrderResult orderResult)
    {
        switch (orderResult)
        {
            case NOT_EXECUTED:
                return ExecutionResult.NOT_EXECUTED;
            case EXECUTED:
                return ExecutionResult.EXECUTED;
            default:
                throw new IllegalArgumentException(orderResult.name());
        }
    }

    private static OrderResult toOrderResult(final ExecutionResult executionResult)
    {
        switch (executionResult)
        {
            case NOT_EXECUTED:
                return OrderResult.NOT_EXECUTED;
            case EXECUTED:
                return OrderResult.EXECUTED;
            default:
                throw new IllegalArgumentException(executionResult.name());
        }
    }

    public int encode(final MarketMessage message, final MutableDirectBuffer buffer, final int offset)
    {
        if (message instanceof FirmPrice)
        {
            FirmPrice firmPrice = (FirmPrice)message;
            firmPriceEncoder.wrapAndApplyHeader(buffer, offset, messageHeaderEncoder)
                    .correlationId(firmPrice.correlationId())
                    .updateTime(firmPrice.updateTime())
                    .bidPrice(firmPrice.bidPrice())
                    .bidQuantity(firmPrice.bidQuantity())
                    .askPrice(firmPrice.askPrice())
                    .askQuantity(firmPrice.askQuantity());
            return messageHeaderEncoder.encodedLength() + firmPriceEncoder.encodedLength();
        }
        if (message instanceof Order)
        {
            Order order = (Order)message;
            orderEncoder.wrapAndApplyHeader(buffer, offset, messageHeaderEncoder)
                    .bidPrice(order.bidPrice())
                    .bidQuantity(order.bidQuantity())
                    .askPrice(order.askPrice())
                    .askQuantity(order.askQuantity());
            return messageHeaderEncoder.encodedLength() + orderEncoder.encodedLength();
        }
        if (message instanceof OrderResult)
        {
            orderResultEncoder.wrapAndApplyHeader(buffer, offset, messageHeaderEncoder);
            orderResultEncoder.result(toExecutionResult((OrderResult)message));
            return messageHeaderEncoder.encodedLength() + orderResultEncoder.encodedLength();
        }
        return 0;
    }

    public MarketMessage decode(final DirectBuffer buffer, final int offset)
    {
        messageHeaderDecoder.wrap(buffer, offset);
        if (messageHeaderDecoder.templateId() == FirmPriceDecoder.TEMPLATE_ID)
        {
            int bodyOffset = offset + messageHeaderDecoder.encodedLength();
            firmPriceDecoder.wrap(
                    buffer,
                    bodyOffset,
                    messageHeaderDecoder.blockLength(),
                    messageHeaderDecoder.version()
            );
            decodedFirmPrice.update(
                    firmPriceDecoder.correlationId(),
                    firmPriceDecoder.updateTime(),
                    firmPriceDecoder.bidPrice(),
                    firmPriceDecoder.bidQuantity(),
                    firmPriceDecoder.askPrice(),
                    firmPriceDecoder.askQuantity()
            );
            return decodedFirmPrice;
        }
        if (messageHeaderDecoder.templateId() == OrderDecoder.TEMPLATE_ID)
        {
            int bodyOffset = offset + messageHeaderDecoder.encodedLength();
            orderDecoder.wrap(
                    buffer,
                    bodyOffset,
                    messageHeaderDecoder.blockLength(),
                    messageHeaderDecoder.version()
            );
            decodedOrder.update(
                    orderDecoder.bidPrice(),
                    orderDecoder.bidQuantity(),
                    orderDecoder.askPrice(),
                    orderDecoder.askQuantity()
            );
            return decodedOrder;
        }
        if (messageHeaderDecoder.templateId() == OrderResultDecoder.TEMPLATE_ID)
        {
            int bodyOffset = offset + messageHeaderDecoder.encodedLength();
            orderResultDecoder.wrap(
                    buffer,
                    bodyOffset,
                    messageHeaderDecoder.blockLength(),
                    messageHeaderDecoder.version()
            );
            return toOrderResult(orderResultDecoder.result());
        }
        return null;
    }
}

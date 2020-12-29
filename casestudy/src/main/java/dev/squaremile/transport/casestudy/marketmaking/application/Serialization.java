package dev.squaremile.transport.casestudy.marketmaking.application;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;


import dev.squaremile.transport.casestudy.marketmaking.domain.ExecutionReport;
import dev.squaremile.transport.casestudy.marketmaking.domain.FirmPrice;
import dev.squaremile.transport.casestudy.marketmaking.domain.HeartBeat;
import dev.squaremile.transport.casestudy.marketmaking.domain.MarketMessage;
import dev.squaremile.transport.casestudy.marketmaking.domain.Order;
import dev.squaremile.transport.casestudy.marketmaking.domain.OrderResult;
import dev.squaremile.transport.casestudy.marketmaking.domain.Security;
import dev.squaremile.transport.casestudy.marketmaking.domain.TrackedSecurity;
import dev.squaremile.transport.casestudy.marketmaking.schema.ExecutionReportDecoder;
import dev.squaremile.transport.casestudy.marketmaking.schema.ExecutionReportEncoder;
import dev.squaremile.transport.casestudy.marketmaking.schema.ExecutionResult;
import dev.squaremile.transport.casestudy.marketmaking.schema.FirmPriceDecoder;
import dev.squaremile.transport.casestudy.marketmaking.schema.FirmPriceEncoder;
import dev.squaremile.transport.casestudy.marketmaking.schema.HeartBeatDecoder;
import dev.squaremile.transport.casestudy.marketmaking.schema.HeartBeatEncoder;
import dev.squaremile.transport.casestudy.marketmaking.schema.MessageHeaderDecoder;
import dev.squaremile.transport.casestudy.marketmaking.schema.MessageHeaderEncoder;
import dev.squaremile.transport.casestudy.marketmaking.schema.OrderDecoder;
import dev.squaremile.transport.casestudy.marketmaking.schema.OrderEncoder;
import dev.squaremile.transport.casestudy.marketmaking.schema.OrderResultDecoder;
import dev.squaremile.transport.casestudy.marketmaking.schema.OrderResultEncoder;
import dev.squaremile.transport.casestudy.marketmaking.schema.SecurityDecoder;
import dev.squaremile.transport.casestudy.marketmaking.schema.SecurityEncoder;

public class Serialization
{
    private final FirmPrice decodedFirmPrice = FirmPrice.createNoPrice();
    private final Order decodedOrder = new Order(0, 0, 0, 0);
    private final ExecutionReport decodedExecutionReport = new ExecutionReport();
    private final TrackedSecurity decodedSecurity = new TrackedSecurity();
    private final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
    private final MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
    private final FirmPriceEncoder firmPriceEncoder = new FirmPriceEncoder();
    private final FirmPriceDecoder firmPriceDecoder = new FirmPriceDecoder();
    private final OrderEncoder orderEncoder = new OrderEncoder();
    private final OrderDecoder orderDecoder = new OrderDecoder();
    private final OrderResultEncoder orderResultEncoder = new OrderResultEncoder();
    private final OrderResultDecoder orderResultDecoder = new OrderResultDecoder();
    private final ExecutionReportDecoder executionReportDecoder = new ExecutionReportDecoder();
    private final ExecutionReportEncoder executionReportEncoder = new ExecutionReportEncoder();
    private final SecurityDecoder securityDecoder = new SecurityDecoder();
    private final SecurityEncoder securityEncoder = new SecurityEncoder();
    private final HeartBeatEncoder heartBeatEncoder = new HeartBeatEncoder();
    private final HeartBeat decodedHeartBeat = new HeartBeat();

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
        if (message instanceof ExecutionReport)
        {
            ExecutionReport executionReport = (ExecutionReport)message;
            executionReportEncoder.wrapAndApplyHeader(buffer, offset, messageHeaderEncoder)
                    .passiveMarketParticipantId(executionReport.passiveTraderId())
                    .aggressiveMarketParticipantId(executionReport.aggressiveTraderId())
                    .midPrice(executionReport.security().midPrice())
                    .lastUpdateTime(executionReport.security().lastUpdateTime())
                    .lastPriceChange(executionReport.security().lastPriceChange())
                    .bidPrice(executionReport.executedOrder().bidPrice())
                    .bidQuantity(executionReport.executedOrder().bidQuantity())
                    .askPrice(executionReport.executedOrder().askPrice())
                    .askQuantity(executionReport.executedOrder().askQuantity());
            return messageHeaderEncoder.encodedLength() + executionReportEncoder.encodedLength();
        }
        if (message instanceof Security)
        {
            Security security = (Security)message;
            securityEncoder.wrapAndApplyHeader(buffer, offset, messageHeaderEncoder)
                    .midPrice(security.midPrice())
                    .lastUpdateTime(security.lastUpdateTime())
                    .lastPriceChange(security.lastPriceChange());
            return messageHeaderEncoder.encodedLength() + securityEncoder.encodedLength();
        }
        if (message instanceof HeartBeat)
        {
            heartBeatEncoder.wrapAndApplyHeader(buffer, offset, messageHeaderEncoder);
            return messageHeaderEncoder.encodedLength() + securityEncoder.encodedLength();
        }
        throw new IllegalArgumentException(message.getClass().getCanonicalName());
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
        if (messageHeaderDecoder.templateId() == ExecutionReportDecoder.TEMPLATE_ID)
        {
            int bodyOffset = offset + messageHeaderDecoder.encodedLength();
            executionReportDecoder.wrap(
                    buffer,
                    bodyOffset,
                    messageHeaderDecoder.blockLength(),
                    messageHeaderDecoder.version()
            );
            decodedSecurity.update(executionReportDecoder.lastUpdateTime(), executionReportDecoder.midPrice(), executionReportDecoder.lastPriceChange());
            decodedOrder.update(executionReportDecoder.bidPrice(), executionReportDecoder.bidQuantity(), executionReportDecoder.askPrice(), executionReportDecoder.askQuantity());
            decodedExecutionReport.update(
                    executionReportDecoder.passiveMarketParticipantId(),
                    executionReportDecoder.aggressiveMarketParticipantId(),
                    decodedSecurity,
                    decodedOrder
            );
            return decodedExecutionReport;
        }
        if (messageHeaderDecoder.templateId() == SecurityDecoder.TEMPLATE_ID)
        {
            int bodyOffset = offset + messageHeaderDecoder.encodedLength();
            securityDecoder.wrap(
                    buffer,
                    bodyOffset,
                    messageHeaderDecoder.blockLength(),
                    messageHeaderDecoder.version()
            );
            decodedSecurity.update(securityDecoder.lastUpdateTime(), securityDecoder.midPrice(), securityDecoder.lastPriceChange());
            return decodedSecurity;
        }
        if (messageHeaderDecoder.templateId() == HeartBeatDecoder.TEMPLATE_ID)
        {
            return decodedHeartBeat;
        }
        return null;
    }
}

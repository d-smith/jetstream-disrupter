package org.ds.jslmax.handlers;

import com.lmax.disruptor.RingBuffer;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import org.ds.jslmax.application.PositionsDisruptor;
import org.ds.jslmax.quotes.QuoteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class QuotesMessageHandler implements MessageHandler {
    private final RingBuffer<QuoteEvent> quoteEventRingBuffer;

    public QuotesMessageHandler(RingBuffer<QuoteEvent> quoteEventRingBuffer) {
        this.quoteEventRingBuffer = quoteEventRingBuffer;
    }

    public static Logger LOG = LoggerFactory.getLogger(QuotesMessageHandler.class);

    @Override
    public void onMessage(Message msg) throws InterruptedException {
        LOG.debug("quote is {}", msg.toString());
        String[] subjectParts = msg.getSubject().split("\\.");
        if(subjectParts.length != 2) {
            LOG.warn("ignoring {}", msg);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(new String(msg.getData(), StandardCharsets.UTF_8));
        } catch(Throwable t) {
            LOG.warn("Unable to parse price in msg {}", msg);
            return;
        }

        long sequenceId = quoteEventRingBuffer.next();
        QuoteEvent quoteEvent = quoteEventRingBuffer.get(sequenceId);
        quoteEvent.symbol = subjectParts[1];
        quoteEvent.price = price;
        quoteEventRingBuffer.publish(sequenceId);
    }
}

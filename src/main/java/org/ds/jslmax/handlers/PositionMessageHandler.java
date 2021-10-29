package org.ds.jslmax.handlers;

import com.lmax.disruptor.RingBuffer;
import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import org.ds.jslmax.positions.Position;
import org.ds.jslmax.quotes.QuoteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PositionMessageHandler implements MessageHandler {
    static Logger LOG = LoggerFactory.getLogger(PositionMessageHandler.class);

    private ArrayList<String> symbols;
    private PositionsMonitor positionsMonitor;
    private JetStream jetStream;
    private RingBuffer<QuoteEvent> ringBuffer;
    private Dispatcher dispatcher;

    public PositionMessageHandler(Connection nc, RingBuffer<QuoteEvent> ringBuffer, PositionsMonitor positionsMonitor) throws IOException {
        symbols = new ArrayList<>();

        this.jetStream =nc.jetStream();
        this.ringBuffer = ringBuffer;
        this.positionsMonitor = positionsMonitor;
        dispatcher = nc.createDispatcher();
    }

    @Override
    public void onMessage(Message msg) throws InterruptedException {
        String rawPosition = new String(msg.getData(), StandardCharsets.UTF_8);
        LOG.info("position is {}", rawPosition);

        String[] tokens = rawPosition.split(",");
        if(tokens.length != 3) {
            LOG.warn("raw position not tokenized into 3 tokens");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(tokens[2]);
        } catch(NumberFormatException nfe) {
            LOG.warn("unable to convert amount token to double");
            return;
        }

        positionsMonitor.addPosition(new Position(tokens[0], tokens[1], amount));

        String symbol = tokens[1];
        if(symbols.contains(symbol)) {
            LOG.info("have seen {}", symbol);
        } else {
            LOG.info("new symbol {} - subscribe dispatcher to quotes.{}", symbol, symbol);
            symbols.add(symbol);
            //quotesDispatcher.subscribe("quotes." + symbol);
            PushSubscribeOptions po = PushSubscribeOptions.builder()
                    .configuration(
                            ConsumerConfiguration.builder()
                                    .deliverPolicy(DeliverPolicy.New)
                                    .build()
                    )
                    .build();
            try {
                jetStream.subscribe("quotes." + symbol,
                        dispatcher,
                        new QuotesMessageHandler(ringBuffer),
                        true,
                        po);
            } catch(Throwable t) {
                LOG.error("Unable to create subscription to {}", "quotes." + symbol);
            }
        }

    }
}

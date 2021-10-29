package org.ds.jslmax.marketval;

import com.lmax.disruptor.EventHandler;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.Message;
import io.nats.client.api.PublishAck;
import io.nats.client.impl.NatsMessage;
import org.ds.jslmax.counters.CompletionCounter;
import org.ds.jslmax.counters.PublishCounter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class MarketValueEventConsumer implements EventHandler<MarketValueEvent> {


    private final String MARKET_VALUE_TOPIC = "mvupdates";
    private JetStream jetStream;
    private CompletionCounter completionCounter;
    private PublishCounter pc;

    public MarketValueEventConsumer(Connection nc) throws IOException {
        this.jetStream = nc.jetStream();
        this.completionCounter = new CompletionCounter();
        this.pc = new PublishCounter();
    }

    public String formatMarketValue(String owner, String symbol, double value) {
        return "MarketValueUpdate{" +
                "owner='" + owner + '\'' +
                ", symbol='" + symbol + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public void onEvent(MarketValueEvent marketValueEvent, long l, boolean b) throws Exception {
        double positionValue = marketValueEvent.position.getAmount() * marketValueEvent.quoteEvent.price;

        Message msg = NatsMessage.builder()
                .subject(MARKET_VALUE_TOPIC)
                .data(formatMarketValue(marketValueEvent.position.getOwner(),
                        marketValueEvent.position.getSymbol(),
                        positionValue).getBytes(StandardCharsets.UTF_8))
                .build();

        CompletableFuture<PublishAck> f = jetStream.publishAsync(msg);
        pc.count();
        f.whenComplete((ack,t)-> {
            completionCounter.count(ack,t);
        });

    }
}

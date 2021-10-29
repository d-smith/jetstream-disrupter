package org.ds.jslmax.quotes;

import com.lmax.disruptor.EventFactory;

import java.time.Instant;

public class QuoteEvent {
    public String symbol;
    public double price;
    public Instant timestamp;

    public final static EventFactory EVENT_FACTORY
            = () -> new QuoteEvent();

    public QuoteEvent() {
        timestamp = Instant.now();
    }

    @Override
    public String toString() {
        return "QuoteEvent{" +
                "symbol='" + symbol + '\'' +
                ", price=" + price +
                ", timestamp=" + timestamp +
                '}';
    }
}

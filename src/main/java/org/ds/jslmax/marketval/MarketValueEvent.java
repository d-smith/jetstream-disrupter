package org.ds.jslmax.marketval;

import com.lmax.disruptor.EventFactory;
import org.ds.jslmax.positions.Position;
import org.ds.jslmax.quotes.QuoteEvent;

public class MarketValueEvent {

    public Position position;
    public QuoteEvent quoteEvent;

    public final static EventFactory EVENT_FACTORY
            = () -> new MarketValueEvent();
}

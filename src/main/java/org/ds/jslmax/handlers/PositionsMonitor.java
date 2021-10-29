package org.ds.jslmax.handlers;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import org.ds.jslmax.marketval.MarketValueEvent;
import org.ds.jslmax.positions.Position;
import org.ds.jslmax.quotes.QuoteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionsMonitor implements EventHandler<QuoteEvent> {
    private static Logger LOG = LoggerFactory.getLogger(PositionsMonitor.class);

    private Map<String, List<Position>> positions;
    private RingBuffer<MarketValueEvent> mvRingBuffer;

    public PositionsMonitor(RingBuffer<MarketValueEvent> mvRingBuffer) {
        this.positions = new HashMap<>();
        this.mvRingBuffer = mvRingBuffer;
    }

    public void addPosition(Position p) {
        LOG.info("adding position {}", p);
        String symbol = p.getSymbol();
        List<Position> positionsForSymbol = positions.get(symbol);
        if(positionsForSymbol == null) {
            positionsForSymbol = new ArrayList<>();
            positions.put(symbol, positionsForSymbol);
        }

        positionsForSymbol.add(p);
    }


    @Override
    public void onEvent(QuoteEvent quoteEvent, long l, boolean b) throws Exception {
        LOG.debug("quote event {}", quoteEvent);

        String symbol = quoteEvent.symbol;
        List<Position> positionsForSymbol = positions.get(symbol);
        positionsForSymbol.forEach(p -> {
            long sequenceId = mvRingBuffer.next();
            MarketValueEvent marketValueEvent = mvRingBuffer.get(sequenceId);
            marketValueEvent.quoteEvent = quoteEvent;
            marketValueEvent.position = p;
            mvRingBuffer.publish(sequenceId);
        });

    }
}

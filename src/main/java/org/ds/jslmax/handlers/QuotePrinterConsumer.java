package org.ds.jslmax.handlers;

import com.lmax.disruptor.EventHandler;
import org.ds.jslmax.quotes.QuoteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuotePrinterConsumer implements EventHandler<QuoteEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(QuotePrinterConsumer.class);

    long called;

    public QuotePrinterConsumer() {
        called = 0;
    }
    @Override
    public void onEvent(QuoteEvent quoteEvent, long seq, boolean b) throws Exception {
        //LOG.info("onEvent sequence is {} value event is {}", seq, quoteEvent);
        called++;
        if(called % 1000 == 0) {
            LOG.info("onEvent called at least {} times so far, current sequence is {}",
                    called, seq);
        }
    }
}

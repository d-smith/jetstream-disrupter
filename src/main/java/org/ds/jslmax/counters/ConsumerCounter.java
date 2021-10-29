package org.ds.jslmax.counters;

import org.ds.jslmax.consumers.QuotesPushConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerCounter {

    private static Logger LOG = LoggerFactory.getLogger(ConsumerCounter.class);

    private AtomicInteger count;
    private long epoch;

    public ConsumerCounter() {
        count = new AtomicInteger();
        epoch = System.currentTimeMillis();
    }

    public void count() {
        int current = count.incrementAndGet();
        if (current % 10000 == 0) {
            long now = System.currentTimeMillis();
            LOG.info("{} consumed in {} ms - {} per second", current, now - epoch, (1000.0 * current) / (now - epoch));
        }
    }
}

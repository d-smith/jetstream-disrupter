package org.ds.jslmax.counters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {

    private static Logger LOG = LoggerFactory.getLogger(Counter.class);

    private AtomicInteger count;
    private long epoch;
    private String logFormatStr;

    public Counter(String logFormatStr) {
        count = new AtomicInteger();
        epoch = System.currentTimeMillis();
        this.logFormatStr = logFormatStr;
    }

    public void count() {
        int current = count.incrementAndGet();
        if (current % 10000 == 0) {
            long now = System.currentTimeMillis();
            LOG.info(logFormatStr, current, now - epoch, (1000.0 * current) / (now - epoch));
        }
    }
}

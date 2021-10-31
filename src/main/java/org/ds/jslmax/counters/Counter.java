package org.ds.jslmax.counters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter {

    private static Logger LOG = LoggerFactory.getLogger(Counter.class);

    private AtomicInteger count;
    private long epoch;
    private String logFormatStr;
    private AtomicBoolean firstCall;

    public Counter(String logFormatStr) {
        count = new AtomicInteger();
        epoch = -1;
        firstCall = new AtomicBoolean(false);
        this.logFormatStr = logFormatStr;
    }

    public void count() {
        if(firstCall.getAndSet(true) == false) {
            epoch = System.currentTimeMillis();
        }
        int current = count.incrementAndGet();
        if (current % 10000 == 0) {
            long now = System.currentTimeMillis();
            LOG.info(logFormatStr, current, now - epoch, (1000.0 * current) / (now - epoch));
        }
    }
}

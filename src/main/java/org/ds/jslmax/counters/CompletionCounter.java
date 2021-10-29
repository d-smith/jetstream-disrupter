package org.ds.jslmax.counters;

import io.nats.client.api.PublishAck;
import org.ds.jslmax.producers.QuotesProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class CompletionCounter {
    private AtomicInteger count;
    private AtomicInteger errorCount;
    private long epoch;

    private Logger LOG = LoggerFactory.getLogger(CompletionCounter.class);

    public CompletionCounter() {
        count = new AtomicInteger();
        errorCount = new AtomicInteger();
        epoch = System.currentTimeMillis();
    }

    public void count(PublishAck publishAck, Throwable throwable) {
        if(publishAck != null) {
            int current = count.incrementAndGet();
            if(current % 10000 == 0) {
                long now = System.currentTimeMillis();
                LOG.info("{} publish futures completed is {} ms - {} per second", current, now - epoch, (1000.0 * current)/(now - epoch));
            }
        } else if (throwable != null) {
            int errors = errorCount.incrementAndGet();
            LOG.info("error {} total errors {}", throwable.getMessage(), errors);
        }
    }
}

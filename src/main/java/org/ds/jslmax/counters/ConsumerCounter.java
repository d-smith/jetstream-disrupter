package org.ds.jslmax.counters;

import org.ds.jslmax.consumers.QuotesPushConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerCounter extends Counter {
    public ConsumerCounter() {
       super("{} consumed in {} ms - {} per second");
    }
}

package org.ds.jslmax.consumers;

import io.nats.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class QuotesPushConsumer {
    static Logger LOG = LoggerFactory.getLogger(QuotesPushConsumer.class);

    public static class ConsumerCounter {
        private AtomicInteger count;
        private long epoch;

        public ConsumerCounter() {
            count = new AtomicInteger();
            epoch = System.currentTimeMillis();
        }

        public void count() {
            int current = count.incrementAndGet();
            if(current % 10000 == 0) {
                long now = System.currentTimeMillis();
                LOG.info("{} consumed in {} ms - {} per second", current, now, (1000.0 * current)/(now - epoch));
            }
        }
    }

    public static void main(String... args) throws Exception {

        LOG.info("Connect to nats...");
        Connection nc = Nats.connect("nats://localhost:4222");
        JetStream js = nc.jetStream();
        LOG.info("Jetstream!");

        /*
        PullSubscribeOptions pullOptions = PullSubscribeOptions.builder()
                .durable("quotes-consumer")
                .build();

        JetStreamSubscription sub = js.subscribe("quotes.*", pullOptions);
         */
        ConsumerCounter counter = new ConsumerCounter();
        Dispatcher dispatcher = nc.createDispatcher();
        MessageHandler handler = (msg) -> {
            counter.count();
        };

        boolean autoAck = true;

        js.subscribe("quotes.>", dispatcher, handler, autoAck);


    }
}

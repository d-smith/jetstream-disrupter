package org.ds.jslmax.consumers;

import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import org.ds.jslmax.counters.ConsumerCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class QuotesPullConsumer {
    static Logger LOG = LoggerFactory.getLogger(QuotesPullConsumer.class);



    public static void main(String... args) throws Exception {

        LOG.info("Connect to nats...");
        Connection nc = Nats.connect("nats://localhost:4222");
        JetStream js = nc.jetStream();
        LOG.info("Jetstream!");


        PullSubscribeOptions pullOptions = PullSubscribeOptions.builder()
                .durable("quotes-consumer-x")
                .configuration(ConsumerConfiguration.builder()
                        .deliverPolicy(DeliverPolicy.New)
                        .build())
                .build();

        JetStreamSubscription sub = js.subscribe("quotes.>", pullOptions);
        ConsumerCounter counter = new ConsumerCounter();

        for(;;) {
            List<Message> messages = sub.fetch(25, Duration.ofMillis(100));
            for(Message m: messages) {
                counter.count();
            }
        }


    }
}

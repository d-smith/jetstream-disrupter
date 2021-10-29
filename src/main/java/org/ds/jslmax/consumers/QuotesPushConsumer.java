package org.ds.jslmax.consumers;

import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import org.ds.jslmax.counters.ConsumerCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuotesPushConsumer {
    static Logger LOG = LoggerFactory.getLogger(QuotesPushConsumer.class);

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

        PushSubscribeOptions po = PushSubscribeOptions.builder()
                .configuration(
                        ConsumerConfiguration.builder()
                                .deliverPolicy(DeliverPolicy.New)
                                .build()
                )
                .build();


        js.subscribe("quotes.>", dispatcher, handler, autoAck, po);


    }
}

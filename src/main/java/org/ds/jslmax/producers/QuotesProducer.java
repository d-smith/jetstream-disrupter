package org.ds.jslmax.producers;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.api.PublishAck;
import io.nats.client.impl.NatsMessage;
import org.ds.jslmax.counters.CompletionCounter;
import org.ds.jslmax.counters.PublishCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class QuotesProducer {
    static Logger LOG = LoggerFactory.getLogger(QuotesProducer.class);

    public static void main(String... args) throws Exception {

        String[] subjects =  {
                "quotes.DWAC",
                "quotes.SNDL",
                "quotes.CEI",
                "quotes.FAMI",
                "quotes.KMDN"
        };

        LOG.info("Connect to nats...");
        Connection nc = Nats.connect("nats://localhost:4222");
        JetStream js = nc.jetStream();
        LOG.info("Jetstream!");

        int count = 0;
        long startTime = System.currentTimeMillis();
        CompletionCounter completionCounter = new CompletionCounter();
        PublishCounter pc = new PublishCounter();

        for(;;) {
            int idx = (int) (Math.random() * subjects.length);
            String subject = subjects[idx];
            byte[] randoPrice = String.valueOf((Math.random() * 600)).getBytes(StandardCharsets.UTF_8);

            Message msg = NatsMessage.builder()
                    .subject(subject)
                    .data(randoPrice)
                    .build();

            CompletableFuture<PublishAck> f = js.publishAsync(msg);
            pc.count();
            f.whenComplete((ack,t)-> {
                completionCounter.count(ack,t);
            });
            Thread.sleep(10);
        }
    }
}

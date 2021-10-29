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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

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

        for(int i = 0; i < 1000000; i++) {
            /*
            count++;
            if(count % 1000 == 0) {
                //LOG.info("count is {}", count);
                long stopTime = System.currentTimeMillis();
                LOG.info("Publishing at {} quotes/sec", count * 1000.0 / (stopTime - startTime));
                startTime = stopTime;
                count = 0;
            }
             */
            int idx = (int) (Math.random() * subjects.length);
            String subject = subjects[idx];
            byte[] randoPrice = String.valueOf((Math.random() * 600)).getBytes(StandardCharsets.UTF_8);

            Message msg = NatsMessage.builder()
                    .subject(subject)
                    .data(randoPrice)
                    .build();


            //PublishAck pa = js.publish(msg);



            CompletableFuture<PublishAck> f = js.publishAsync(msg);
            pc.count();
            f.whenComplete((ack,t)-> {
                completionCounter.count(ack,t);
            });
            /*
            f.whenComplete((ack,t)-> {
                if(t != null) {
                    t.printStackTrace();
                }
              //LOG.info("ack is {} t is {}", ack != null ? ack.toString() : ack, t == null ? "" : t.getMessage());
            });

             */

        }
    }
}

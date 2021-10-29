package org.ds.jslmax.producers;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.api.PublishAck;
import io.nats.client.impl.NatsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class QuotesProducer {
    static Logger LOG = LoggerFactory.getLogger(QuotesProducer.class);

    public static class PublishCounter {
        private AtomicInteger count;
        private long epoch;

        public PublishCounter() {
            count = new AtomicInteger();
            epoch = System.currentTimeMillis();
        }

        public void count() {
            int current = count.incrementAndGet();
            if(current % 10000 == 0) {
                long now = System.currentTimeMillis();
                LOG.info("{} published in {} ms - {} per second", current, now - epoch, (1000.0 * current)/(now - epoch));
            }
        }
    }

    public static class CompletionCounter  {
        private AtomicInteger count;
        private AtomicInteger errorCount;
        private long epoch;

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

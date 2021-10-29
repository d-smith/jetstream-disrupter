package org.ds.jslmax.application;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import org.ds.jslmax.handlers.PositionMessageHandler;
import org.ds.jslmax.handlers.PositionsMonitor;
import org.ds.jslmax.handlers.QuotePrinterConsumer;
import org.ds.jslmax.handlers.QuotesMessageHandler;
import org.ds.jslmax.marketval.MarketValueEvent;
import org.ds.jslmax.marketval.MarketValueEventConsumer;
import org.ds.jslmax.quotes.QuoteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadFactory;

public class PositionsDisruptor {
    private static final Logger LOG = LoggerFactory.getLogger(PositionsDisruptor.class);

    public static void main(String... args) throws Exception {
        LOG.info("alive");
        String natsEndpoints = System.getenv("NATS_ENDPOINT");
        if (natsEndpoints == null) {
            LOG.warn("NATS_ENDPOINT not set in environment... defaulting to nats://localhost:4222");
            natsEndpoints = "nats://localhost:4222";
        }

        //Need a nats connection
        Connection natsConnection = Nats.connect(natsEndpoints);

        ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;
        WaitStrategy waitStrategy = new BusySpinWaitStrategy();

        //
        // Market position value disruptor
        //
        Disruptor<MarketValueEvent> mvDisruptor = new Disruptor<>(
                MarketValueEvent.EVENT_FACTORY,
                512,
                threadFactory,
                ProducerType.SINGLE,
                waitStrategy
        );

        ArrayList<EventHandler> mvHandlers = new ArrayList<>();
        mvHandlers.add(new MarketValueEventConsumer(natsConnection));
        mvDisruptor.handleEventsWith(mvHandlers.toArray(new EventHandler[0]));
        RingBuffer<MarketValueEvent> mvRingBuffer = mvDisruptor.start();

        //Need disrupter setup


        Disruptor<QuoteEvent> disruptor
                = new Disruptor<>(
                QuoteEvent.EVENT_FACTORY,
                256,
                threadFactory,
                ProducerType.SINGLE,
                waitStrategy);

        PositionsMonitor positionsMonitor = new PositionsMonitor(mvRingBuffer);

        ArrayList<EventHandler> handlers = new ArrayList<>();
        //handlers.add(new QuotePrinterConsumer());
        handlers.add(positionsMonitor);
        disruptor.handleEventsWith(handlers.toArray(new EventHandler[0]));

        RingBuffer<QuoteEvent> ringBuffer = disruptor.start();

        configurePositionsSubscription (natsConnection, ringBuffer, positionsMonitor);
    }

    private static void configurePositionsSubscription(Connection natsConnection, RingBuffer<QuoteEvent> ringBuffer, PositionsMonitor positionsMonitor) throws IOException, JetStreamApiException {
        Dispatcher positionsDispatcher = natsConnection.createDispatcher();
        final boolean autoAck = true;

        PushSubscribeOptions po = PushSubscribeOptions.builder()
                .configuration(
                        ConsumerConfiguration.builder()
                                .deliverPolicy(DeliverPolicy.New)
                                .build()
                )
                .build();
        natsConnection.jetStream().subscribe("positions",
                positionsDispatcher,
                new PositionMessageHandler(natsConnection, ringBuffer, positionsMonitor),
                autoAck,
                po);
    }
}

# Jetstream Disruptor

Combine the forces of NAT.io Jetstream and lmax distruptor

## Set Up

Running jetstream server:

```
docker run --name nats-main -p 4222:4222 nats -js
docker run --network host -p 4222:4222 nats -js
```

Run jetstream utilities

```
$ brew tap nats-io/nats-tools
$ brew install nats-io/nats-tools/nats
nats context add local --description "Localhost" --select
```

Create a quotes stream, and a quote stream consumer using the utils

```
docker-desktop:~# nats str add QUOTES
? Subjects to consume quotes.*
? Storage backend file
? Retention Policy Limits
? Discard Policy Old
? Stream Messages Limit -1
? Per Subject Messages Limit -1
? Message size limit -1
? Maximum message age limit -1
? Maximum individual message size -1
? Duplicate tracking time window 2m
? Replicas 1
Stream QUOTES was created

Information for Stream QUOTES created 2021-10-28T20:48:29Z

Configuration:

             Subjects: quotes.*
     Acknowledgements: true
            Retention: File - Limits
             Replicas: 1
       Discard Policy: Old
     Duplicate Window: 2m0s
     Maximum Messages: unlimited
        Maximum Bytes: unlimited
          Maximum Age: 0.00s
 Maximum Message Size: unlimited
    Maximum Consumers: unlimited


State:

             Messages: 0
                Bytes: 0 B
             FirstSeq: 0
              LastSeq: 0
     Active Consumers: 0
     
     
docker-desktop:~# nats con add
? Consumer name quotes-consumer
? Delivery target (empty for Pull Consumers) 
? Start policy (all, new, last, subject, 1h, msg sequence) new
? Replay policy instant
? Filter Stream by subject (blank for all) 
? Maximum Allowed Deliveries -1
? Maximum Acknowledgements Pending 0
? Select a Stream QUOTES
Information for Consumer QUOTES > quotes-consumer created 2021-10-28T20:53:02Z

Configuration:

        Durable Name: quotes-consumer
           Pull Mode: true
        Deliver Next: true
          Ack Policy: Explicit
            Ack Wait: 30s
       Replay Policy: Instant
     Max Ack Pending: 20,000
   Max Waiting Pulls: 512

State:

   Last Delivered Message: Consumer sequence: 0 Stream sequence: 0
     Acknowledgment floor: Consumer sequence: 0 Stream sequence: 0
         Outstanding Acks: 0 out of maximum 20000
     Redelivered Messages: 0
     Unprocessed Messages: 0
            Waiting Pulls: 0 of maximum 512
```
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

Create a quotes stream:

```
nats str create --discard=old --max-msgs=10000000 --retention=limits --storage=memory --subjects=quotes.* QUOTES
```




## NATS.io docs

Java client - see [here](https://github.com/nats-io/nats.java)
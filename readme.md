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
### File backed streams

Create a quotes stream:

```
nats str create --discard=old --max-msgs=1000000 --retention=limits --storage=file --subjects="quotes.*" QUOTES
```

Create a positions stream

```
nats str create --discard=old --max-msgs=100000 --retention=limits --storage=file --subjects=positions POSITIONS
```

Create a market value stream

```
nats str create --discard=old --max-msgs=1000000 --retention=limits --storage=file --subjects=mvupdates MVSTR
```

### Memory backed streams

```
nats str create --discard=old --max-msgs=100000 --retention=limits --storage=memory --subjects="quotes.*" QUOTES

nats str create --discard=old --max-msgs=100000 --retention=limits --storage=memory --subjects=positions POSITIONS

nats str create --discard=old --max-msgs=100000 --retention=limits --storage=memory --subjects=mvupdates MVSTR
```

## NATS.io docs

Java client - see [here](https://github.com/nats-io/nats.java)
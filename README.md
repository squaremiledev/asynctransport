# squaremile.dev - AsyncTransport

[![Tests_Java_8_11 Build Status](https://github.com/squaremiledev/asynctransport/workflows/Tests_Java_8_11/badge.svg)](https://github.com/squaremiledev/asynctransport/actions?query=workflow%3ATests_Java_8_11)

The main artifact of the Async Transport family is AsyncTCP.
AsyncTCP is a fully composable library to be used as a building block for
a low latency, message-driven, high performance TCP applications written in Java.

## Key features:

- compatible with Java 8 and above

- no external dependencies, apart from some Agrona data structures

- purely message driven, with all messages serializable for an audit and replay purposes

- zero allocation in steady state to avoid GC pauses 

- single threaded, single CPU utilisation even for multiple connections

- low latency with single digit microseconds overhead even at hundreds of thousands messages a second on commodity hardware

- no Java NIO/Socket knowledge necessary to use it efficiently

- easily extendable, with some extensions under active development (Aeron Cluster, Chronicle Queue, Ring Buffer, TCP endpoints simulators)

- ideal for event-sourced systems
 

## Evaluation

### Add dependencies

Maven

```
<dependency>
	<groupId>dev.squaremile</groupId>
	<artifactId>asynctcp</artifactId>
	<version>0.7.0</version>
	<type>pom</type>
</dependency>
```

Gradle

```
implementation 'dev.squaremile:asynctcp:0.7.0'
```

### See some examples

To start write your own application, see dev.squaremile.asynctcpacceptance.AppListeningOnTcpPort class.

To use TCP in a message-driven fashion, look at `dev.squaremile.asynctcp.DeterministicTransportApplicationTest`

The easiest way to start evaluating if the library can be of some use is to

### Check, that the library works

To build:

`make`

To run sample app after built:

```
./asynctcpacceptance/build/distributions/asynctcpacceptance/bin/asynctcpacceptance 9999
# In the new shell:
telnet localhost 9999
```

### Confirm that this design style is applicable to a particular context.

Go to dev.squaremile.asynctcpacceptance.AppListeningOnTcpPort or try to implement your own Application

## Design objectives

The library is built, and enables applications to be built, according to the Object Oriented Programming paradigm,
following as closely as possible Alan Kay's definition of "messaging, local retention and protection and hiding of state-process"

It is also inspired by the [Aeron Cluster](https://github.com/real-logic/aeron/tree/master/aeron-cluster)'s boundaries
between the application and the infrastructure (ClusteredService interface).
However, an attempt has been made to make this library even more composable
and avoid cycles during the setup phase (problematic `onStart(Cluster cluster)`).
This is to encourage some good functional programming principles during the construction/configuration phase,
such as immutability and no side effects. Mutability is introduced only when it is
necessary to meet performance requirements.  

## Performance

Coordinated omission included in all the results.

### Localhost, round trip

200 000 msg/s, load distributed 50/50 between sending and receiving.

- Intel i7-4770, 16GB RAM DDR3

- (A) 100k msg/s send -> (B) 100k msg/s received -> (B) 100k msg/s send -> (A) 100k msg/s received

``` 
Scenario: remoteHost localhost, remotePort 9998, sendingRatePerSecond 100000, skippedWarmUpResponses 31250 , messagesSent 6000000, 187500 expected responses with a response rate 1 for 32, use buffers: true, extra data 0 bytes
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |             9 |             18 |
99th percentile        |            15 |             30 |
99.9th percentile      |            27 |             54 |
99.99th percentile     |            64 |            127 |
99.999th percentile    |           164 |            328 |
worst                  |           220 |            439 |

Based on 156250 measurements.
It took 49999 ms between the first measured message sent and the last received

```

### Cloud (AWS EC2)

500 000 msg/s, load skewed 1/1000 (one box mostly sends, another mostly receives)

- c5n.xlarge boxes, same availability zone, ping rtt min/avg/max/mdev = 0.059/0.068/0.273/0.013 ms

- Ubuntu 20.04.1 LTS, Java OpenJDK Runtime Environment (build 11.0.9+11-Ubuntu-0ubuntu1.20.04)

- (A) 500k msg/s send -> (B) 500k msg/s received -> (B) 500 msg/s send -> (A) 500 msg/s received

Command run on box ip-172-31-35-37:

```
trcheck-shadow-0.8.0-SNAPSHOT/bin/trcheck benchmark server -p 9998
```

Command run on box ip-172-31-43-169:

```
trcheck-shadow-0.8.0-SNAPSHOT/bin/trcheck benchmark client -h 172.31.35.37 -p 9998 --warm-up-time=30 --run-time=30 --send-rate=500000 --respond-rate=1000 --extra-data-length=140
```

First run

```
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            41 |             82 |
99th percentile        |            78 |            156 |
99.9th percentile      |            97 |            193 |
99.99th percentile     |           111 |            222 |
99.999th percentile    |           111 |            222 |
worst                  |           111 |            222 |
```

Second run
```

Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            41 |             82 |
99th percentile        |            74 |            148 |
99.9th percentile      |            93 |            185 |
99.99th percentile     |           100 |            199 |
99.999th percentile    |           102 |            203 |
worst                  |           102 |            203 |
```

Third run
```
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            40 |             79 |
99th percentile        |            68 |            136 |
99.9th percentile      |            83 |            166 |
99.99th percentile     |            92 |            183 |
99.999th percentile    |            96 |            191 |
worst                  |            96 |            191 |
```

Each run based on 15000 measurements.
It took 29998 ms between the first measured message sent and the last received
Sent total (including warm up) 29999028 messages of average size (TCP headers excluded) 164 bytes
Sent total (including warm up) 4919840264 bytes with a throughput of 656.022 Mbps

### Summary

- Average overhead of the library (vs ICMP ping) is between 6 and 7 microseconds one way.

- The latency is stable when the connection has warmed up (enough time to calculate TCP window size etc.)

## Usage

This is all is needed to create an app that listens on appPort TCP port and sends Hi! to whoever connected.

You can go to asynctcpacceptance/src/main/java/dev/squaremile/asynctcpacceptance/demo/AppFromReadme.java and run the example.

```java
package dev.squaremile.asynctcpacceptance.demo;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.api.transport.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.app.Event;
import dev.squaremile.asynctcp.api.transport.app.TransportApplicationOnDuty;
import dev.squaremile.asynctcp.api.transport.commands.Listen;
import dev.squaremile.asynctcp.api.transport.commands.SendData;
import dev.squaremile.asynctcp.api.transport.events.ConnectionAccepted;

import static dev.squaremile.asynctcp.api.serialization.PredefinedTransportDelineation.rawStreaming;

public class AppFromReadme
{
    public static void main(String[] args)
    {
        ApplicationOnDuty app = new AsyncTcp().createSharedStack(
                "MyApp",
                transport -> new TransportApplicationOnDuty()
                {

                    @Override
                    public void onStart()
                    {
                        transport.handle(transport.command(Listen.class).set(1, 8889, rawStreaming()));
                        System.out.println("now you can run `telnet localhost 8889` in the terminal");
                    }

                    @Override
                    public void onStop()
                    {
                    }

                    @Override
                    public void onEvent(final Event event)
                    {
                        System.out.println(event);
                        if (event instanceof ConnectionAccepted)
                        {
                            ConnectionAccepted connectionAccepted = (ConnectionAccepted)event;
                            transport.handle(transport.command(connectionAccepted.connectionId(), SendData.class).set("Hi!".getBytes()));
                        }
                    }
                }
        );

        app.onStart();
        while (true)
        {
            app.work();
        }
    }
}
```

The user provided implementation can be passed using a factory to the launcher.
The remaining concerns, such as running the app are kept separate and can be composed from the building blocks.


## Maintenance

To re-generate sbe codecs

```
./gradlew :asynctcp:sbeGenerateJavaCodecs :usecases:sbeGenerateJavaCodecs
```

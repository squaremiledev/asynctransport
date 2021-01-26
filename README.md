# squaremile.dev - AsyncTransport

[![Tests_Java_8_11 Build Status](https://github.com/squaremiledev/asynctransport/workflows/Tests_Java_8_11/badge.svg)](https://github.com/squaremiledev/asynctransport/actions?query=workflow%3ATests_Java_8_11)

The main artifact of the Async Transport family is AsyncTCP (dev.squaremile:asynctcp).
AsyncTCP is a fully composable library to be used as a building block for
a low latency, message-driven, high performance TCP applications written in Java.

## Key features:

- compatible with Java 8 and above

- no external dependencies, apart from some Agrona data structures

- purely message driven, with all messages optionally serialized for an audit and replay purposes

- message delineation functionality provided

- zero allocation in steady state to avoid GC pauses 

- single threaded, single CPU utilisation even for multiple connections

- single digit microseconds latency end to end TCP connection for up to hundreds of thousands messages a second on a commodity hardware

- microseconds latency end to end TCP connection for up to millions of messages a second

- no Java NIO/Socket knowledge necessary to use it efficiently

- easily extendable, with some extensions under active development (Aeron Cluster, Chronicle Queue, Ring Buffer, TCP endpoints simulators)

- ideal for event-sourced systems

- useful for measuring an impact of the underlying network/hosts topology on the latency distribution (trcheck tool) 

## Evaluation

### Add dependencies

Maven

```
<dependency>
	<groupId>dev.squaremile</groupId>
	<artifactId>asynctcp</artifactId>
	<version>0.9.0</version>
	<type>pom</type>
</dependency>
```

Gradle

```
implementation 'dev.squaremile:asynctcp:0.9.0'
```

### See some examples

To start write your own application, see `dev.squaremile.asynctcpacceptance.AppListeningOnTcpPort` class.

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

## Design objectives

The library is built, and enables applications to be built, according to the Object Oriented Programming paradigm,
following as closely as possible Alan Kay's definition of "messaging, local retention and protection and hiding of state-process"

It is also inspired by the [Aeron Cluster](https://github.com/real-logic/aeron/tree/master/aeron-cluster)'s boundaries
between the application and the infrastructure (ClusteredService interface).
However, an attempt has been made to make this library even more composable
and avoid cycles during the setup phase (such as `onStart(Cluster cluster)`).
This is to encourage some good functional programming principles during the construction/configuration phase,
such as immutability and no side effects. Mutability is introduced only to meet the performance requirements.  

## Performance

In order to test the latencies, a simple echo protocol has been introduced that timestamps messages, adds some additional data,
sends the messages at the predefined rate to another endpoint and compares the encoded timestamp with the current time when after the full round trip
the finally arrive at the source again. Coordinated omission is taken into account and included in all the results, so when the message publication is delayed for any reason,
the time when the message was supposed to be sent is used as the start time, as this is the real delay that would be experienced on the receiving side.

### Localhost, round trip

Scenario: A single box exchanges small messages over localhost. Used to measure the library overhead only, without any network overhead.

- OpenJDK Runtime Environment (build 1.8.0_275-8u275-b01-0ubuntu1~20.04-b01)

- CPU: Intel(R) Core(TM) i7-10750H CPU @ 2.60GHz

#### Medium rate, 50 000 msg/s over TCP

```bash
# First command run:
./trcheck/build/distributions/trcheck-shadow-0.9.0/bin/trcheck benchmark server -p 9998

# Second command run:
./trcheck/build/distributions/trcheck-shadow-0.9.0/bin/trcheck benchmark client -h localhost -p 9998 -t 30 -w 20 -s 50000 -r 1 -x 0
```

```
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |             5 |             10 |
99th percentile        |             6 |             12 |
99.9th percentile      |             9 |             18 |
99.99th percentile     |            14 |             27 |
99.999th percentile    |            73 |            145 |
worst                  |            98 |            195 |

Based on 1500000 measurements.
It took 30000 ms between the first measured message sent and the last received
Sent total (including warm up) 2500000 messages of average size (TCP headers excluded) 24 bytes
Sent total (including warm up) 60000000 bytes with a throughput of 9.600 Mbps
```

#### High rate, 5 million msg/s over TCP

```bash
# First command run:
./trcheck/build/distributions/trcheck-shadow-0.9.0/bin/trcheck benchmark server -p 9998

# Second command run:
./trcheck/build/distributions/trcheck-shadow-0.9.0/bin/trcheck benchmark client -h localhost -p 9998 -t 30 -w 20 -s 5000000 -r 5000 -x 0
```

```
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            69 |            138 |
99th percentile        |            94 |            187 |
99.9th percentile      |            97 |            194 |
99.99th percentile     |           262 |            524 |
99.999th percentile    |           347 |            694 |
worst                  |           347 |            694 |

Based on 30000 measurements.
It took 29999 ms between the first measured message sent and the last received
Sent total (including warm up) 249995842 messages of average size (TCP headers excluded) 24 bytes
Sent total (including warm up) 5999900208 bytes with a throughput of 960.022 Mbps
```

### Cloud (AWS EC2)

Scenario: 2 Boxes with a network connection between them adding an average ping of ~ 70 microseconds 

- Instance: c5n.xlarge boxes

- same availability zone, ping rtt min/avg/max/mdev = 0.060/0.068/0.118/0.007 ms

- Ubuntu 20.04.1 LTS, Java OpenJDK Runtime Environment (build 11.0.9+11-Ubuntu-0ubuntu1.20.04)

#### Medium rate, 60 000 msg/s over TCP

```bash
# Command run on box ip-172-31-35-37:
trcheck-shadow-0.9.0/bin/trcheck benchmark server -p 9998

# Command run on box ip-172-31-43-169:
trcheck-shadow-0.9.0/bin/trcheck benchmark client -h 172.31.35.37 -p 9998     --warm-up-time=30 --run-time=200 --send-rate=60000 --respond-rate=1000 --extra-data-length=0
```

```
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            39 |             78 |
99th percentile        |            58 |            116 |
99.9th percentile      |            63 |            126 |
99.99th percentile     |            79 |            158 |
99.999th percentile    |           111 |            222 |
worst                  |           111 |            222 |

Based on 12000 measurements.
It took 199975 ms between the first measured message sent and the last received
Sent total (including warm up) 13799004 messages of average size (TCP headers excluded) 24 bytes
Sent total (including warm up) 331176096 bytes with a throughput of 11.521 Mbps
```

#### High rate, 3 million msg/s over TCP

- 3 million msg/s, load skewed 1/3000 (one box mostly sends, another mostly receives)

- (A) 3M msg/s send -> (B) 3M msg/s received -> (B) 1k msg/s send -> (A) 1k msg/s received

```bash
# Command run on box ip-172-31-35-37:
trcheck-shadow-0.9.0/bin/trcheck benchmark server -p 9998

# Command run on box ip-172-31-43-169:
trcheck-shadow-0.9.0/bin/trcheck benchmark client -h 172.31.35.37 -p 9998     --warm-up-time=30 --run-time=20 --send-rate=3000000 --respond-rate=3000 --extra-data-length=0
```

```
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            83 |            165 |
99th percentile        |           108 |            215 |
99.9th percentile      |           127 |            254 |
99.99th percentile     |           132 |            263 |
99.999th percentile    |           137 |            274 |
worst                  |           137 |            274 |

Based on 20000 measurements.
It took 19979 ms between the first measured message sent and the last received
Sent total (including warm up) 149997493 messages of average size (TCP headers excluded) 24 bytes
Sent total (including warm up) 3599939832 bytes with a throughput of 576.590 Mbps
```

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

To re-generate sbe codecs and build the project

```
./gradlew :asynctcp:sbeGenerateJavaCodecs && make
```

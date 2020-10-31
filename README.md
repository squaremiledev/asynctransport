# squaremile.dev - AsyncTransport

[![Tests_Java_8_11 Build Status](https://github.com/squaremiledev/asynctransport/workflows/Tests_Java_8_11/badge.svg)](https://github.com/squaremiledev/asynctransport/actions?query=workflow%3ATests_Java_8_11)

This library is an attempt to build a fully composable, library-style (read, non-framework style),
message-driven, high performance, single threaded tcp server/client.

It has been primarily designed to be used as a building block for other tools that require some audit
capabilities, such as test spies and that should not slow down systems under test.

However, because of a relatively low latency even at higher message rates, it can be also used as a building block for
event sourced systems where part of its communication is done via TCP.

To start write your own application, see dev.squaremile.asynctcpacceptance.AppListeningOnTcpPort class.

To use TCP in a message-driven fashion, look at `dev.squaremile.asynctcpacceptance.TcpOverRingBufferTest`

## Evaluation

The easiest way to start evaluating if the library can be of some use is to

### 1. Check if it works

To build:

`make`

To run sample app after built:

```
./asynctcpacceptance/build/distributions/asynctcpacceptance/bin/asynctcpacceptance 9999
# In the new shell:
telnet localhost 9999
```


### 2. confirm that this design style is applicable to a particular context.

Go to dev.squaremile.asynctcpacceptance.AppListeningOnTcpPort or try to implement your own Application

### 3. confirm that the overhead is acceptable to be used in a specific context

Let's say you want to test what kind o latency you should expect when sending 100 000 small messages a second on you local machine.

build the library with

`make`

Run the echo server

`java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.EchoConnectionApplication  9998`

Run the message source server

`java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.SourcingConnectionApplication localhost 9998 100_000 31250 6000000 32`

Where localhost 9998 100000 31250 6000000 32 is remote_host port messageSendingRatePerSecond skippedWarmUpResponses messagesSent sendToReceiveRatio

## Design objectives

The library is built, and enables applications to be built, according to the Object Oriented Programming paradigm,
following as closely as possible Alan Kay's definition of "messaging, local retention and protection and hiding of state-process"

It is also inspired by the [Aeron Cluster](https://github.com/real-logic/aeron/tree/master/aeron-cluster)'s boundaries
between the application and the infrastructure (ClusteredService interface).
However, an attempt has been made to make this library even more composable
and avoid cycles during the setup phase (problematic `onStart(Cluster cluster)`).

## Performance

Coordinated omission included in all the results.

### Localhost, round trip

200 000 msg/s, load distributed 50/50 between sending and receiving.

- Intel i7-4770, 16GB RAM DDR3

- (A) 100k msg/s send -> (B) 100k msg/s received -> (B) 100k msg/s send -> (A) 100k msg/s received

```
Scenario: remoteHost localhost, remotePort 9998, sendingRatePerSecond 100000, skippedWarmUpResponses 31250 , messagesSent 6000000, 187500 expected responses with a response rate 1 for 32
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |             8 |             15 |
99th percentile        |            15 |             29 |
99.9th percentile      |            29 |             58 |
99.99th percentile     |            63 |            126 |
99.999th percentile    |           132 |            264 |
worst                  |           143 |            285 |

Based on 156250 measurements.
It took 49999 ms between the first measured message sent and the last received
```

### Cloud (AWS EC2)

264 000 msg/s, load skewed 1/32 (one box mostly sends, another mostly receives)

- c5n.xlarge boxes, same availability zone, ping ~50 microseconds between boxes

- (A) 256k msg/s send -> (B) 256k msg/s received -> (B) 8k msg/s send -> (A) 8k msg/s received

```
Scenario: remoteHost 172.31.35.37, remotePort 9998, sendingRatePerSecond 256000, skippedWarmUpResponses 80000 , messagesSent 25600000, 800000 expected responses with a response rate 1 for 32
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            34 |             68 |
99th percentile        |            48 |             96 |
99.9th percentile      |            55 |            109 |
99.99th percentile     |            79 |            157 |
99.999th percentile    |           112 |            224 |
worst                  |           419 |            838 |

Based on 720000 measurements.
It took 89994 ms between the first measured message sent and the last received
```

For more measurements with different modes, see [test results for AWS EC2 boxes](docs/aws.md).

## Usage

This is all is needed to create an app that listens on appPort TCP port and sends Hi! to whoever connected.

You can go to asynctcpacceptance/src/main/java/dev/squaremile/asynctcpacceptance/demo/AppFromReadme.java and run the example.

```java
package dev.squaremile.asynctcpacceptance.demo;

import dev.squaremile.asynctcp.api.AsyncTcp;
import dev.squaremile.asynctcp.transport.api.app.ApplicationOnDuty;
import dev.squaremile.asynctcp.transport.api.app.Event;
import dev.squaremile.asynctcp.transport.api.app.EventDrivenApplication;
import dev.squaremile.asynctcp.transport.api.commands.Listen;
import dev.squaremile.asynctcp.transport.api.commands.SendData;
import dev.squaremile.asynctcp.transport.api.events.ConnectionAccepted;

import static dev.squaremile.asynctcp.api.FactoryType.NON_PROD_GRADE;
import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.rawStreaming;

public class AppFromReadme
{
    public static void main(String[] args)
    {
        ApplicationOnDuty app = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
                "MyApp",
                transport -> new EventDrivenApplication()
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
                            transport.handle(transport.command(connectionAccepted, SendData.class).set("Hi!".getBytes()));
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
./gradlew :sbe:sbeGenerateJavaCodecs
```

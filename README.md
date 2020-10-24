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

The easiest way to start evaluating if the library can be of some use is to

1. Check if it works

To build:

`make`

To run sample app after built:

```
./asynctcpacceptance/build/distributions/asynctcpacceptance/bin/asynctcpacceptance 9999
# In the new shell:
telnet localhost 9999
```


2. confirm that this design style is applicable to a particular context.

Go to dev.squaremile.asynctcpacceptance.AppListeningOnTcpPort or try to implement your own Application

3. confirm that the overhead is acceptable to be used in a specific context

build it with `make` and then on one box (or locally) run

`java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.EchoConnectionApplication  9998`

and on another box run

`java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.SourcingConnectionApplication localhost 9998 1000 2000 8000`

Where localhost 9998 1000 2000 8000 is remote_host port messageSendingRatePerSecond numberOfWarmUpMessages numberOfMeasuredMessages

```
Scenario: remoteHost localhost, remotePort 8889, sendingRatePerSecond 48000, warmUpMessages 480000 , measuredMessages 2880000
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |             6 |             11 |
99th percentile        |            10 |             19 |
99.9th percentile      |            14 |             28 |
99.99th percentile     |            37 |             73 |
99.999th percentile    |            70 |            140 |
worst                  |           120 |            239 |

Based on 2880000 measurements.
It took 59999 ms between the first measured message sent and the last received
```

More realistic use case with a reliable connection (same data center) yields results closer to double digit microseconds latency for 99.9th percentile
with ping between boxes being in a double digit microseconds range as well.

When measured against the ping and netperf benchmarks, on a AWS EC2 c5n.xlarge box

- at a rate of ~ 100 000 messages a second using the library is virtually free (unable to detect any overhead)
- at a rate of ~ 200 000 messages a second the overhead starts being visible, with still double digit microseconds latency for 99th percentile but triple digit microseconds latency for 99.9th

Detailed [test results for AWS EC2 boxes are available here.](docs/aws.md)

## Design objectives

The library is built, and enables applications to be built, according to the Object Oriented Programming paradigm,
following as closely as possible Alan Kay's definition of "messaging, local retention and protection and hiding of state-process"

It is also inspired by the [Aeron Cluster](https://github.com/real-logic/aeron/tree/master/aeron-cluster)'s boundaries
between the application and the infrastructure (ClusteredService interface).
However, an attempt has been made to make this library even more composable
and avoid cycles during the setup phase (problematic `onStart(Cluster cluster)`).

## Performance

For localhost (to show the library overhead and ignore out network card/network impact)
the round trip end to end (a message send via the library, received on
the other end by the library, echoed back and received at source) mean time was 111 microseconds for a 99.999th percentile.
running at the rate of 50 000 small msg /s RoundTripTimeSeparateAppTest was used. Coordinated omission was taken into account.

```
Exchanged 8000000 messages at a rate of 95947 messages per second  which took 83 seconds
99.99th percentile is 105 microseconds for a round trip
```

The library should sustain significant load from multiple simultaneous connections.
A combination of EchoConnectionApplication and SourcingConnectionApplication can be used to measure how the performance
changes when more connections are added and load increased.

For reference, below can be observed that that 2 clients and one server, with on average total ~200 000 messages
a second in flight incur 90 microsecond latency one way for 99.99th percentile, sending/receiving 72 000 000 messages
and running  for ~12 minutes.

```
Exchanged 72000000 messages at a rate of 95995 messages per second  which took 750 seconds
99.99th percentile is 198 microseconds for a round trip

Exchanged 72000000 messages at a rate of 95996 messages per second  which took 750 seconds
99.99th percentile is 166 microseconds for a round trip
```

For cloud deployments see [test results for AWS EC2 boxes](docs/aws.md).

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
import static dev.squaremile.asynctcp.serialization.api.PredefinedTransportDelineation.RAW_STREAMING;

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
                        transport.handle(transport.command(Listen.class).set(1, 8889, RAW_STREAMING.type));
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

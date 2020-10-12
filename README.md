# squaremile.dev - AsyncTransport

[![Tests_Java_8_11 Build Status](https://github.com/squaremiledev/asynctransport/workflows/Tests_Java_8_11/badge.svg)](https://github.com/squaremiledev/asynctransport/actions?query=workflow%3ATests_Java_8_11)

This library is an attempt to build a fully composable, library-style (read, non-framework style),
message-driven, high performance, single threaded tcp server/client.

This library is best suited for event sourced systems that must provide a predictable latency
even at a high rate of messages and that part of its communication is done via TCP.

The current overhead of the library that has been measured is ~5 microseconds one way for most of the messages
and ~50 microseconds one way at 99.99th percentile at the rate of ~100 000 small messages a second.

To build:

make

To run sample app after built:

```
./asynctcpacceptance/build/distributions/asynctcpacceptance/bin/asynctcpacceptance 9999
# In the new shell:
telnet localhost 9999
```

To write your own application, see dev.squaremile.asynctcpacceptance.AppListeningOnTcpPort class.

To use TCP in a message-driven fashion, look at `dev.squaremile.asynctcpacceptance.TcpOverDirectBufferTest`

## Design objectives

This library is an attempt to build a fully composable, library-style (read, non-framework style),
message-driven, high performance, single threaded tcp server/client.

It is inspired by the [Aeron Cluster](https://github.com/real-logic/aeron/tree/master/aeron-cluster)'s boundaries
between the application and the infrastructure (ClusteredService interface).
However, an attempt has been made to make this library even more composable
and avoid cycles during the setup phase (problematic onStart(Cluster cluster)).

The current performance should be acceptable for most use cases.
For localhost (to show the library overhead and ignore out network card/network impact)
the round trip end to end (a message send via the library, received on
the other end by the library, echoed back and received at source) mean time was 111 microseconds for a 99.999th percentile.
running at the rate of 50 000 small msg /s RoundTripTimeSeparateAppTest was used. Coordinated omission was taken into account.
Numbers should improve when allocation rate is reduced.

```
Exchanged 8000000 messages at a rate of 95947 messages per second  which took 83 seconds
99.99th percentile is 105 microseconds for a round trip
```

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

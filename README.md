# squaremile.dev - AsyncTransport

[![Tests_Java_8_11 Build Status](https://github.com/squaremiledev/asynctransport/workflows/Tests_Java_8_11/badge.svg)](https://github.com/squaremiledev/asynctransport/actions?query=workflow%3ATests_Java_8_11)

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

The current performance should be acceptable for most use cases. For localhost (to show the library overhead and ignore out network card/network impact)
the round trip end to end (a message send via the library, received on
the other end by the library, echoed back and received at source) mean time is 11 microseconds, and 81 microseconds for a 99.999th percentile.

There are a few optimization tasks in the back log. Tests will need to be repeated on various networks.
RoundTripTimeTest was used. Values are in microseconds, on localhost.

```
Value     Percentile TotalCount 1/(1-Percentile)

11.000 0.900000000000    9503668          10.00
20.000 0.990625000000    9908793         106.67
26.000 0.999023437500    9990277        1024.00
39.000 0.999902343750    9999087       10240.00
81.000 0.999990844727    9999910      109226.67
2587.000 0.999999046326    9999991     1048576.00
3279.000 0.999999904633   10000000    10485760.00
3279.000 1.000000000000   10000000
#[Mean    =       11.071, StdDeviation   =        3.120]
#[Max     =     3279.000, Total count    =     10000000]
```

## Usage

This is all is needed to create an app that listens on appPort TCP port and sends Hi! to whoever connected.

```java

Application app = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
        "MyApp",
        transport -> new Application()
        {
            @Override
            public void onStart()
            {
                transport.handle(transport.command(Listen.class).set(1, 8889, RAW_STREAMING.type));
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

            @Override
            public void onStop()
            {
            }
        }
);

app.onStart();
while (true)
{
    app.work();
}

// more advanced version: asynctcpacceptance/src/main/java/dev/squaremile/asynctcpacceptance/AppListeningOnTcpPort.java

```

The user provided implementation can be passed using a factory to the launcher.
The remaining concerns, such as running the app are kept separate and can be composed from the building blocks.


## Maintenance

To re-generate sbe codecs

```
./gradlew :sbe:sbeGenerateJavaCodecs
```

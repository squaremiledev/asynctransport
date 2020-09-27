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
The construction of the app is as simple as:

```java

TransportApplication app = new AsyncTcp().transportAppFactory(NON_PROD_GRADE).create(
    "AppListeningOnTcpPort",
    transport -> new Application()
    {
        @Override
        public void onStart()
        {
            System.out.println("START");
            transport.handle(transport.command(Listen.class).set(1, appPort));
        }

        @Override
        public void onStop()
        {
            System.out.println("STOP");
        }

        @Override
        public void onEvent(final Event event)
        {
            System.out.println(event);
        }

        @Override
        public void work()
        {
            transport.work();
        }
    }
);

// full version: asynctcpacceptance/src/main/java/dev/squaremile/asynctcpacceptance/AppListeningOnTcpPort.java

```

The user provided implementation can be passed using a factory to the launcher.
The remaining concerns, such as running the app are kept separate and can be composed from the building blocks.


## Maintenance

To re-generate sbe codecs

```
./gradlew :sbe:sbeGenerateJavaCodecs
```

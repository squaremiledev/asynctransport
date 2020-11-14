## Box / Network spec

- c5n.xlarge boxes (2 of them)
- Same availability zone, ping ~50 microseconds between boxes
- Appropriate TCP ports (9998, 12865 for netpers, and some more required by netperf) are open between boxes
- OS: Ubuntu 20.04.1 LTS
- openjdk version "11.0.8" 2020-07-14
- Netperf version 2.6.0
- Nodelay used on both endpoints for Netperf and AsyncTCP

### Ping statistics

```
--- 172.31.43.169 ping statistics ---
100 packets transmitted, 100 received, 0% packet loss, time 20193ms
rtt min/avg/max/mdev = 0.052/0.059/0.104/0.006 ms
```

### Netperf statistics

```
Minimum      Mean         Maximum      99th         Stddev       Transaction
Latency      Latency      Latency      Percentile   Latency      Rate
Microseconds Microseconds Microseconds Latency      Microseconds Tran/s
Microseconds
38           56.69        383          69           3.89         52866.251
```

## Results
```
Performed 530000 transactions at a rate of 53005 transactions per second  which took 9999 milliseconds
mean is               48.76851320754717 microseconds for a round trip
99th percentile is    56 microseconds for a round trip
99.9th percentile is  67 microseconds for a round trip
99.99th percentile is 124 microseconds for a round trip
worst is              256 microseconds for a round trip
std deviation is      3.1420 microseconds
```

## More data

### Multiple Netperf runs

```
ubuntu@ip-172-31-35-37:$ time netperf -H 172.31.43.169 -p 12865 -l -600000 -t TCP_RR -j -- -r 16 -w1 -b 2 -D -O min_latency,mean_latency,max_latency,p99_latency,stddev_latency,transaction_rate
MIGRATED TCP REQUEST/RESPONSE TEST from 0.0.0.0 (0.0.0.0) port 0 AF_INET to 172.31.43.169 () port 0 AF_INET : nodelay : demo : first burst 2
Minimum      Mean         Maximum      99th         Stddev       Transaction
Latency      Latency      Latency      Percentile   Latency      Rate
Microseconds Microseconds Microseconds Latency      Microseconds Tran/s
Microseconds
38           56.69        383          69           3.89         52866.251

real	0m11.387s
user	0m0.765s
sys	0m4.851s

ubuntu@ip-172-31-35-37:$ time netperf -H 172.31.43.169 -p 12865 -l -600000 -t TCP_RR -j -- -r 16 -w1 -b 2 -D -O min_latency,mean_latency,max_latency,p99_latency,stddev_latency,transaction_rate
MIGRATED TCP REQUEST/RESPONSE TEST from 0.0.0.0 (0.0.0.0) port 0 AF_INET to 172.31.43.169 () port 0 AF_INET : nodelay : demo : first burst 2
Minimum      Mean         Maximum      99th         Stddev       Transaction
Latency      Latency      Latency      Percentile   Latency      Rate
Microseconds Microseconds Microseconds Latency      Microseconds Tran/s
Microseconds
42           57.68        187          70           4.03         51949.295

real	0m11.589s
user	0m0.806s
sys	0m4.698s

ubuntu@ip-172-31-35-37:$ time netperf -H 172.31.43.169 -p 12865 -l -600000 -t TCP_RR -j -- -r 16 -w1 -b 2 -D -O min_latency,mean_latency,max_latency,p99_latency,stddev_latency,transaction_rate
MIGRATED TCP REQUEST/RESPONSE TEST from 0.0.0.0 (0.0.0.0) port 0 AF_INET to 172.31.43.169 () port 0 AF_INET : nodelay : demo : first burst 2
Minimum      Mean         Maximum      99th         Stddev       Transaction
Latency      Latency      Latency      Percentile   Latency      Rate
Microseconds Microseconds Microseconds Latency      Microseconds Tran/s
Microseconds
41           57.24        231          65           2.32         52350.201

real	0m11.499s
user	0m0.938s
sys	0m5.727s

ubuntu@ip-172-31-35-37:$ time netperf -H 172.31.43.169 -p 12865 -l -600000 -t TCP_RR -j -- -r 16 -w1 -b 2 -D -O min_latency,mean_latency,max_latency,p99_latency,stddev_latency,transaction_rate
MIGRATED TCP REQUEST/RESPONSE TEST from 0.0.0.0 (0.0.0.0) port 0 AF_INET to 172.31.43.169 () port 0 AF_INET : nodelay : demo : first burst 2
Minimum      Mean         Maximum      99th         Stddev       Transaction
Latency      Latency      Latency      Percentile   Latency      Rate
Microseconds Microseconds Microseconds Latency      Microseconds Tran/s
Microseconds
37           59.13        318          78           6.80         50679.480

real	0m11.880s
user	0m0.930s
sys	0m5.096s
```

### AsyncTcp

Rate 1/1 fo messages received/sent (doubles the number of messages on the wire and requires constnt  switching between reading and writing from the socket)

```
ubuntu@ip-172-31-35-37:$ java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.SourcingConnectionApplication 172.31.43.169 9998 53000 53000 530000
Starting with remoteHost 172.31.43.169, remotePort 9998, sendingRatePerSecond 53000, warmUpMessages 53000 , measuredMessages 530000

Exchanged 1060000 messages at a rate of 106010 messages per second  which took 9999 milliseconds
Performed 530000 transactions at a rate of 53005 transactions per second  which took 9999 milliseconds
mean is               48.76851320754717 microseconds for a round trip
99th percentile is    56 microseconds for a round trip
99.9th percentile is  67 microseconds for a round trip
99.99th percentile is 124 microseconds for a round trip
worst is              256 microseconds for a round trip
std deviation is      3.1420 microseconds

ubuntu@ip-172-31-35-37:$ java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.SourcingConnectionApplication 172.31.43.169 9998 53000 53000 530000
Starting with remoteHost 172.31.43.169, remotePort 9998, sendingRatePerSecond 53000, warmUpMessages 53000 , measuredMessages 530000

Exchanged 1060000 messages at a rate of 106010 messages per second  which took 9999 milliseconds
Performed 530000 transactions at a rate of 53005 transactions per second  which took 9999 milliseconds
mean is               46.58045471698113 microseconds for a round trip
99th percentile is    53 microseconds for a round trip
99.9th percentile is  65 microseconds for a round trip
99.99th percentile is 111 microseconds for a round trip
worst is              261 microseconds for a round trip
std deviation is      2.5491 microseconds

ubuntu@ip-172-31-35-37:$ java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.SourcingConnectionApplication 172.31.43.169 9998 53000 53000 530000
Starting with remoteHost 172.31.43.169, remotePort 9998, sendingRatePerSecond 53000, warmUpMessages 53000 , measuredMessages 530000

Exchanged 1060000 messages at a rate of 106010 messages per second  which took 9999 milliseconds
Performed 530000 transactions at a rate of 53005 transactions per second  which took 9999 milliseconds
mean is               45.566303773584906 microseconds for a round trip
99th percentile is    50 microseconds for a round trip
99.9th percentile is  56 microseconds for a round trip
99.99th percentile is 92 microseconds for a round trip
worst is              247 microseconds for a round trip
std deviation is      1.5818 microseconds

ubuntu@ip-172-31-35-37:$ java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.SourcingConnectionApplication 172.31.43.169 9998 53000 53000 530000
Starting with remoteHost 172.31.43.169, remotePort 9998, sendingRatePerSecond 53000, warmUpMessages 53000 , measuredMessages 530000

Exchanged 1060000 messages at a rate of 106010 messages per second  which took 9999 milliseconds
Performed 530000 transactions at a rate of 53005 transactions per second  which took 9999 milliseconds
mean is               45.94460754716981 microseconds for a round trip
99th percentile is    56 microseconds for a round trip
99.9th percentile is  62 microseconds for a round trip
99.99th percentile is 107 microseconds for a round trip
worst is              270 microseconds for a round trip
std deviation is      3.6119 microseconds
```



## Higher throughput

AsyncTCP

1/1 messages received/sent ratio

```
java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.SourcingConnectionApplication 172.31.43.169 9998 100000 1000000 1000000
Starting with remoteHost 172.31.43.169, remotePort 9998, sendingRatePerSecond 100000, warmUpMessages 1000000 , measuredMessages 1000000

Exchanged 2000000 messages at a rate of 200020 messages per second  which took 9999 milliseconds
Performed 1000000 transactions at a rate of 100010 transactions per second  which took 9999 milliseconds
mean is               60.990538 microseconds for a round trip
99th percentile is    77 microseconds for a round trip
99.9th percentile is  551 microseconds for a round trip
99.99th percentile is 1174 microseconds for a round trip
worst is              1317 microseconds for a round trip
std deviation is      29.721 microseconds
```

~0.25M messages a second, 1/32 messages received/sent ratio (measures a round trip time when the load is skewed in one direction)

```
java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.SourcingConnectionApplication 172.31.35.37 9998 256000 80000 25600000 32

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

## With serialization (ring buffers)

Test v. cbd74f0a2edb0e27f3d868ccbc99e0b2598bddc7

```
# box 1
java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.EchoApplication 9998

# box 2
java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.SourcingConnectionApplication 172.31.35.37 9998 256000 80000 5600000 32 1
Scenario: remoteHost 172.31.35.37, remotePort 9998, sendingRatePerSecond 256000, skippedWarmUpResponses 80000 , messagesSent 5600000, 175000 expected responses with a response rate 1 for 32, use buffers: true
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            30 |             60 |
99th percentile        |            44 |             88 |
99.9th percentile      |            49 |             98 |
99.99th percentile     |            70 |            139 |
99.999th percentile    |           130 |            260 |
worst                  |           130 |            260 |

Based on 95000 measurements.
It took 11874 ms between the first measured message sent and the last received
```

Netperf benchmark

```
ubuntu@ip-172-31-35-37:$ time netperf -H 172.31.43.169 -p 12865 -l -1600000 -t TCP_RR -j -- -r 16 -w1 -b 8 -D -O min_latency,mean_latency,max_latency,p99_latency,stddev_latency,transaction_rate
MIGRATED TCP REQUEST/RESPONSE TEST from 0.0.0.0 (0.0.0.0) port 0 AF_INET to 172.31.43.169 () port 0 AF_INET : nodelay : demo : first burst 8
Minimum      Mean         Maximum      99th         Stddev       Transaction
Latency      Latency      Latency      Percentile   Latency      Rate
Microseconds Microseconds Microseconds Latency      Microseconds Tran/s
Microseconds
39           67.52        517          87           7.94         133132.823
```


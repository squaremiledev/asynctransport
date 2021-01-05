## Box / Network spec

- c5n.xlarge boxes (2 of them)
- Same availability zone
- Appropriate TCP ports (9998, 12865 for netpers, and some more required by netperf) are open between boxes
- OS: Ubuntu 20.04.1 LTS
- openjdk version "11.0.8" 2020-07-14
- Netperf version 2.6.0
- Nodelay used on both endpoints for Netperf and AsyncTCP

### Ping statistics

```
--- 172.31.35.37 ping statistics ---
445 packets transmitted, 445 received, 0% packet loss, time 90575ms
rtt min/avg/max/mdev = 0.052/0.064/0.167/0.008 ms
```

### AsyncTcp

Using AsyncTCP library with a high message rate (600 000 messages a second).

At this rate the overhead of using the library is on average ~8 microseconds and
worst ~40 microseconds (one way) ICMP (ping) traffic. 


```
Box A:
ubuntu@ip-172-31-35-37:~/workspace/asynctransport-master$ java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.EchoApplication 9998

Box B:
ubuntu@ip-172-31-43-169:~/workspace/asynctransport-master$ java -classpath "./asynctcpacceptance/build/distributions/asynctcpacceptance/lib/*" dev.squaremile.asynctcpacceptance.SourcingConnectionApplication 172.31.35.37 9998 600000 60000 12000000 100 1 64

Scenario: remoteHost 172.31.35.37, remotePort 9998, sendingRatePerSecond 600000, skippedWarmUpResponses 60000 , messagesSent 12000000, 120000 expected responses with a response rate 1 for 100, use buffers: true, extra data 64 bytes

Based on 60000 measurements.
It took 9995 ms between the first measured message sent and the last received

Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            40 |             80 |
99th percentile        |            74 |            148 |
99.9th percentile      |           100 |            200 |
99.99th percentile     |           110 |            219 |
99.999th percentile    |           128 |            255 |
worst                  |           128 |            255 |

Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            41 |             81 |
99th percentile        |            76 |            151 |
99.9th percentile      |            93 |            185 |
99.99th percentile     |           106 |            211 |
99.999th percentile    |           136 |            271 |
worst                  |           136 |            271 |

Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            40 |             80 |
99th percentile        |            74 |            148 |
99.9th percentile      |            93 |            186 |
99.99th percentile     |           104 |            207 |
99.999th percentile    |           110 |            219 |
worst                  |           110 |            219 |
```

### Netperf statistics

```
Minimum      Mean         Maximum      99th         Stddev       Transaction
Latency      Latency      Latency      Percentile   Latency      Rate
Microseconds Microseconds Microseconds Latency      Microseconds Tran/s
Microseconds
38           56.69        383          69           3.89         52866.251
```

Multiple Netperf runs

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


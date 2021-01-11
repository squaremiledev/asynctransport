I wanted to measure the overhead of running bpftrace whilst the app is running.
I've used market data-like profile as I think it's the one that the most interesting due to its latency-sensitive nature and of higher than other workflows volume.

## Test scenario:

Send 20 000 msg/s 4Kb each and measure latency distribution when bpftrace is

A) off (benchmark),

B) ON with nothing interesting to log,

C) ON, logging all the packets

## Specs:

- 2 c5n.xlarge boxes within the same availability zone, with ping between them rtt min/avg/max/mdev = 0.065/0.074/0.127/0.010 ms

- openjdk version "11.0.8" 2020-07-14


## Details:

- [bpftrace script used](tcpwindow.bt)

- market data simulator used: tcpcheck ping

- Scenario details: remoteHost 172.31.35.37, remotePort 9998, sendingRatePerSecond 20000, skippedWarmUpResponses 10000 , messagesSent 1000000, 31250 expected responses with a response rate 1 for 32, use buffers: true, extra data 4000 bytes

## Results

### A) bpftrace is not running (benchmark)

```
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            36 |             72 |
99th percentile        |            64 |            127 |
99.9th percentile      |            88 |            175 |
99.99th percentile     |           103 |            206 |
99.999th percentile    |           104 |            207 |
worst                  |           104 |            207 |
Based on 21250 measurements.
It took 33998 ms between the first measured message sent and the last received

---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            32 |             64 |
99th percentile        |            70 |            139 |
99.9th percentile      |            93 |            185 |
99.99th percentile     |           103 |            205 |
99.999th percentile    |           106 |            212 |
worst                  |           106 |            212 |
Based on 21250 measurements.
It took 33998 ms between the first measured message sent and the last received

Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            33 |             66 |
99th percentile        |            61 |            122 |
99.9th percentile      |            81 |            162 |
99.99th percentile     |            90 |            180 |
99.999th percentile    |           196 |            391 |
worst                  |           196 |            391 |
Based on 21250 measurements.
It took 33998 ms between the first measured message sent and the last received
```

### B) bpftrace is running, but not logging anything (no win0 issues)

```
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            32 |             64 |
99th percentile        |            49 |             98 |
99.9th percentile      |            88 |            176 |
99.99th percentile     |           115 |            229 |
99.999th percentile    |           142 |            283 |
worst                  |           142 |            283 |

Based on 21250 measurements.
It took 33998 ms between the first measured message sent and the last received

Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            36 |             72 |
99th percentile        |            61 |            122 |
99.9th percentile      |            82 |            164 |
99.99th percentile     |            91 |            181 |
99.999th percentile    |           100 |            199 |
worst                  |           100 |            199 |

Based on 21250 measurements.
It took 33998 ms between the first measured message sent and the last received

Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            35 |             69 |
99th percentile        |            76 |            152 |
99.9th percentile      |            96 |            192 |
99.99th percentile     |           106 |            211 |
99.999th percentile    |           248 |            496 |
worst                  |           248 |            496 |

Based on 21250 measurements.
It took 33998 ms between the first measured message sent and the last received
```

### C) bpftrace is running and logging everything to a file

- logged total 356M of logs

- Logged lines similar to:

`[INFO]|2020-11-01 19:05:46|rcv_wnd:62848  |snd_wnd:642304 |[::ffff:172.31.43.169:45856 -> ::ffff:172.31.35.37:9998]|PID: 0|CMD: swapper/2      |rcv_nxt:3193921520,snd_nxt:2269182291,bytes_received:500000,bytes_sent:4015883536,bytes_acked:4015875505`


```
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            59 |            118 |
99th percentile        |            89 |            177 |
99.9th percentile      |           121 |            242 |
99.99th percentile     |           131 |            262 |
99.999th percentile    |           139 |            277 |
worst                  |           139 |            277 |

Based on 21250 measurements.
It took 33998 ms between the first measured message sent and the last received

Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            54 |            107 |
99th percentile        |            81 |            161 |
99.9th percentile      |           108 |            215 |
99.99th percentile     |           125 |            250 |
99.999th percentile    |           129 |            258 |
worst                  |           129 |            258 |

Based on 21250 measurements.
It took 33998 ms between the first measured message sent and the last received


Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            54 |            108 |
99th percentile        |            82 |            164 |
99.9th percentile      |           106 |            211 |
99.99th percentile     |           115 |            229 |
99.999th percentile    |           129 |            257 |
worst                  |           129 |            257 |

Based on 21250 measurements.
It took 33998 ms between the first measured message sent and the last received
```

### D) same as A), repeated again

```
Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            32 |             63 |
99th percentile        |            46 |             91 |
99.9th percentile      |            55 |            109 |
99.99th percentile     |            67 |            133 |
99.999th percentile    |            78 |            155 |
worst                  |            78 |            155 |

Based on 21250 measurements.
It took 33998 ms between the first measured message sent and the last received

Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            32 |             63 |
99th percentile        |            61 |            121 |
99.9th percentile      |            80 |            159 |
99.99th percentile     |            88 |            176 |
99.999th percentile    |           122 |            243 |
worst                  |           122 |            243 |

Based on 21250 measurements.
It took 33998 ms between the first measured message sent and the last received

Results:
---------------------------------------------------------
latency (microseconds) |     ~ one way |     round trip |
mean                   |            34 |             67 |
99th percentile        |            73 |            145 |
99.9th percentile      |            97 |            194 |
99.99th percentile     |           108 |            215 |
99.999th percentile    |           109 |            217 |
worst                  |           109 |            217 |

Based on 21250 measurements.
It took 33998 ms between the first measured message sent and the last received
```

## Conclusion

Even if Bpftrace has any impact, it seems than other factors (cloud environment, OS jitter, JVM, imperfect app) may
by more important
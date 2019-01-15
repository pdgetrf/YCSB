## Run etcd

From ETCD directory:

```
  bin/etcd --advertise-client-urls 'http://0.0.0.0:2379,http://0.0.0.0:4001' --listen-client-urls 'http://0.0.0.0:2379,http://0.0.0.0:4001'
```

## To compile

From YCSB directory:

```
  mvn clean package -pl etcd -am -DskipTests
```

## To test in shell

From YCSB directory:
	
```
  bin/ycsb shell etcd -p hosts="http://127.0.0.1:2379"
```

Example session:

```
Connected.
> help
Commands:
  read key [field1 field2 ...] - Read a record
  scan key recordcount [field1 field2 ...] - Scan starting at key
  insert key name1=value1 [name2=value2 ...] - Insert a new record
  update key name1=value1 [name2=value2 ...] - Update a record
  delete key - Delete a record
  table [tablename] - Get or [set] the name of the table
  quit - Quit
> insert x a=1 b=2 # data is inserted into etcd as /x/a and /x/b
Result: OK
328 ms
> read x a
Return code: OK
a=1
12 ms
> read x b
Return code: OK
b=2
3 ms
> update x b=3
Result: OK
4 ms
> read x b
Return code: OK
b=3
4 ms
> read x a
Return code: OK
a=1
4 ms
> delete x # this will delete all record under /x
Return result: OK
21 ms
> read x a
Return code: NOT_FOUND
5 ms
> read x b
Return code: NOT_FOUND
5 ms
> quit
~/work/YCSB$
```

## To load tests

```
size=10000

bin/ycsb load etcd -p hosts="http://127.0.0.1:2379" -s -P workloads/workloada -threads 4 -p operationcount=$size -p recordcount=$size
```

## To run tests

```
size=10000

bin/ycsb run etcd -p hosts="http://127.0.0.1:2379" -s -P workloads/workloada -threads 4 -p operationcount=$size -p recordcount=$size -p exportfile=/tmp/output.txt -p dataintegrity=true
```

It is okay to run test multiple times with the same load.

## Data validation

In the output.txt producted by 'run etcd', the following seciton shows up with "-p dataintegrity=true" in the run command above.

```
[VERIFY], Operations, 24993
[VERIFY], AverageLatency(us), 28.84443644220382
[VERIFY], MinLatency(us), 13
[VERIFY], MaxLatency(us), 3593
[VERIFY], 95thPercentileLatency(us), 43
[VERIFY], 99thPercentileLatency(us), 60
[VERIFY], Return=OK, 24993
```
This shows data binding is working correctly with the tests. Without dataintegrity (default to false), the [VERIFY] section does not show up, and random data is used for testing instead. 

## Performance tuning

1. An initial tuning is in https://github.com/pdgetrf/YCSB/tree/add_etcd_binding_perf/etcd branch. Reached about 290 ops/sec

2. Added a local cache. ~320 ops/sec

3. Add buik (https://github.com/pdgetrf/YCSB/tree/add_etcd_binding_perf_bulk/etcd), Reached about 360 ops/sec

## Known Issues

1. The shell CRUD commands take a key and a set of key value pairs. Doesn't seem to map directly to the key value pair of ETCD. Something is off (RESOLVED)

2. Load test runs successfully but run test is failing. Need to understand what data is being used (RESOLVED)

3. Data is not validated correctly (RESOLVED)


4. Diagnose etcd performance (TO DO)

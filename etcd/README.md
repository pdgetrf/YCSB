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
>
> insert x a=1 b=2 c=3
Result: OK
389 ms
> read x a b
Return code: OK
a=1
b=2
16 ms
> read x a b
Return code: OK
a=1
b=2
0 ms
> update x b=9
Result: OK
4 ms
> read x a b c
Return code: OK
a=1
b=2
c=3
0 ms
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

bin/ycsb run etcd -p hosts="http://127.0.0.1:2379" -s -P workloads/workloada -threads 4 -p operationcount=$size -p recordcount=$size -p exportfile=/tmp/output.txt
```

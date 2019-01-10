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
> read /a/b/c
Return code: NOT_FOUND
334 ms
> insert /a/b/c =hello # note there are TWO spaces before "=" and no space between "=" and "world". same with update
Result: OK
7 ms
> read /a/b/c
Return code: OK
/a/b/c=hello
4 ms
> update /a/b/c =world
Result: OK
3 ms
> read /a/b/c
Return code: OK
/a/b/c=world
3 ms
> delete /a/b/c
Return result: OK
9 ms
> read /a/b/c
Return code: NOT_FOUND
2 ms
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

## Issues

1. The shell CRUD commands take a key and a set of key value pairs. Doesn't seem to map directly to the key value pair of ETCD. Something is off (RESOLVED)

2. Load test runs successfully but run test is failing. Need to understand what data is being used (RESOLVED)

3. The "table" command is missing (may not need)

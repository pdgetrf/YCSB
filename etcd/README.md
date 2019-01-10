## To compile

From YCSB directory:

	mvn clean package -pl etcd -am -DskipTests

## To test in shell

From YCSB directory:
	
	bin/ycsb shell etcd -p hosts="http://127.0.0.1:2379"

Example session:

----------------------
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
> insert key a=b c=d
Result: OK
322 ms
> read key c
Return code: OK
c=d
11 ms
> update key c=x
Result: OK
4 ms
> read key c
Return code: OK
c=x
3 ms
> delete c
Return result: OK
7 ms
> read key c
Return code: OK
6 ms
----------------------

## To load tests

bin/ycsb load ignite -p hosts="127.0.0.1" -s -P workloads/workloada -threads 4 -p operationcount=100000 -p recordcount=100000

## To run tests

bin/ycsb run etcd -p hosts="http://127.0.0.1:2379" -s -P workloads/workloada -threads 4 -p operationcount=100000 -p recordcount=100000 -p exportfile=/tmp/output.txt


## Issues

1. The shell CRUD commands take a key and a set of key value pairs. Doesn't seem to map directly to the key value pair of ETCD. Something is off.

2. Load test runs successfully but run test is failing. Need to understand what data is being used.

3. The "table" command is missing. 

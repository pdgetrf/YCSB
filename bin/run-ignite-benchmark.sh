echo ">>> load ycsb data"
size=10000 && bin/ycsb load ignite -p hosts="127.0.0.1" -s -P workloads/workloada -threads 1 -p operationcount=$size -p recordcount=$size -p exportfile=/tmp/ignite-ycsb-benchmark-01/ignite-load-1thread-10000keys.txt
sleep 10;

for t in 1 16 32 48 64 80; do
    for i in 1 2 3; do
        echo ">>> running ignite benchmark with ${t} threads attempt ${i}"
        size=10000 && bin/ycsb run ignite -p hosts="127.0.0.1" -s -P workloads/workloada -threads ${t} -p operationcount=$size -p recordcount=$size -p exportfile=/tmp/ignite-ycsb-benchmark-01/ignite-run-${t}thread-10000keys-${i}.txt
        echo ">>> sleep for 10 seconds"
        sleep 10;
    done;
done;

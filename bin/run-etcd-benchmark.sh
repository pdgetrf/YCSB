echo ">>> load ycsb data"
size=10000 && bin/ycsb load etcd -p hosts="http://127.0.0.1:2379" -s -P workloads/workloada -threads 1 -p operationcount=$size -p recordcount=$size -p exportfile=/tmp/etcd-ycsb-benchmark-01/etcd-load-1thread-10000keys.txt
sleep 10;

for t in 1 16 32 48 64 80; do
    for i in 1 2 3; do
        echo ">>> running benchmark with ${t} threads attempt ${i}"
        size=10000 && bin/ycsb run etcd -p hosts="http://127.0.0.1:2379" -s -P workloads/workloada -threads ${t} -p operationcount=$size -p recordcount=$size -p exportfile=/tmp/etcd-ycsb-benchmark-01/etcd-run-${t}thread-10000keys-${i}.txt
        echo ">>> sleep for 10 seconds"
        sleep 10;
    done;
done;

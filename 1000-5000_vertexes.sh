#!/usr/bin/env bash

vertexes=(1000 2000 3000 4000 5000)

for item in ${vertexes[*]}
do
    for n in {1..5}; do
        java -Xmx4g -XX:+PrintGCTimeStamps -XX:+PrintGCDetails \
        -Xloggc:gc_${item}_$2_$3.log -jar shortest-path.jar $item $1 $2 $3;
    done;
done
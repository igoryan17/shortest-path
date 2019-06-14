#!/usr/bin/env bash

vertexes=(20 30 40 50)

for item in ${vertexes[*]}
do
    for n in {1..5}; do
        java -Xmx4g -jar shortest-path.jar $item $1 $2 $3;
    done;
done
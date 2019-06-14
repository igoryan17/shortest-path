#!/usr/bin/env bash

vertexes=(100 150 200)

for item in ${vertexes[*]}
do
    for n in {1..5}; do
        java -Xmx4g -jar shortest-path.jar $item $1 $2 $3;
    done;
done
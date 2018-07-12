#!/bin/bash

exit 0

for dataset in tweets higgs syn1 syn2 syn3 syn4; do
   for p in 0.10 0.08 0.06 0.04 0.02 0.01 0.005 0.001; do
       hdfs dfs -cat  /input/$dataset.table.txt.$p   | wc -l
   done;
done;


#!/bin/bash


exit 0

for dataset in syn1 syn2 syn3 syn4 higgs tweets; do
  for p in  0.001 0.005 0.01 0.02 0.04 0.06 0.08 0.10 100 ; do
         otd=~/results/$dataset
         mkdir -p $otd

         ./download-from-hdfs.sh "/output/"$dataset".table.txt."$p".bulk_counts.txt" $otd/$dataset.$p.txt
  done;
done;

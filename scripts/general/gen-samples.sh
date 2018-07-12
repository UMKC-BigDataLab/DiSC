#!/bin/bash

exit 0

tables="tweet-table-splits/tweets.table.txt syn/syn1/syn1.table.txt syn/syn2/syn2.table.txt syn/syn3/syn3.table.txt syn/syn4/syn4.table.txt  "

samples="0.10 0.08 0.06 0.04 0.02 0.01 0.001"

mkdir samples

for t in $tables; do
    for s in $samples; do
        mkdir -p "samples/"$s"-samples"
        fn=$(echo $t | rev | cut -d '/' -f1 | rev)".$s"
        echo $fn
        ./sample-dataset.py $t 200000000 $s > "samples/"$s"-samples/"$fn
    done;
done;


for t in higgs/higgs.table.txt; do
    for s in $samples; do
        mkdir -p "samples/"$s"-samples"
        fn=$(echo $t | rev | cut -d '/' -f1 | rev)".$s"
        echo $fn
        ./sample-dataset.py $t 176000000 $s > "samples/"$s"-samples/"$fn
    done;
done;

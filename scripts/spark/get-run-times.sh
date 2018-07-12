#!/bin/bash

exit 0

for dataset in higgs tweets syn1 syn2 syn3 syn4; do
    echo $dataset
	for i in 0.10 0.08 0.06 0.04 0.02 0.01 0.005 0.001; do  
        echo -n $i"  "
		cat "run-times/"$dataset.$i.timing.txt  | grep counted 
    done;
    echo ""
done;


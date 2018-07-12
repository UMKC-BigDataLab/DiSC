#!/bin/bash

exit 0

for dataset in tweets higgs syn1 syn2 syn3 syn4; do
for p in 0.10 0.08 0.06 0.04 0.02 0.01 0.005 0.001; do

echo -n $dataset.$p"  "


fact=$(echo "scale = 20; (1/$p)" | bc)
./mrss-relErr.py ~/results/$dataset/$dataset.$p.txt ~/results/$dataset/$dataset.100.txt $fact > ~/results/relerrs/$dataset.$p.relerr.txt  

tail -n 1  ~/results/relerrs/$dataset.$p.relerr.txt  | cut -d ':' -f2

done

done



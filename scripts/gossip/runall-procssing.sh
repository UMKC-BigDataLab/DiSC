#!/bin/bash

log=$1
num=$2

mkdir -p ../run/$num
python new-process-log.py $num $log glbl-part config.txt  &> prg.txt 
python cal-lost-packets.py $log >> ../run/$num/output.txt

startI=$(grep 'First Node' ../run/$num/output.txt | cut -d 't' -f 3 | tr -d ' ')
endI=$(grep 'Last  Node' ../run/$num/output.txt | cut -d 't' -f 3 | tr -d ' ')
python plot-visible-families.py $log ../run/$num  $startI','$endI >> ../run/$num/output.txt 2> prg.vis.txt

echo -n "Average number of families per received packet: " >> ../run/$num/output.txt
grep 'RECEIVED: ' $log | cut -d ':' -f5 | cut -d ' ' -f2 | awk '{ total += $1 } END { print total/NR }' >> ../run/$num/output.txt

echo -n "Average gossip count per family: " >> ../run/$num/output.txt
grep 'Families Gossip Counts ' $log  | cut -d '[' -f 2 | sed 's/]/,/g' | sed 's/:/\n/g' | sed '1d' | cut -d ',' -f1 | awk '{ total += $1 } END { print total/NR }' >> ../run/$num/output.txt

grep 'consideration' $log | sed "s/dropping consideration stats //g" | sed "s/DEBUG //g" > ../run/$num/visible-families-stats.txt 
sameDstCnt=$(grep 'SameDest=True' $log | wc -l )'.0' 
DstCnt=$(grep 'SameDest' $log | wc -l )'.0'
percent=$(echo "print  ('{0:.2f}'.format($sameDstCnt / $DstCnt))" | python)
echo "Percentage of packets set to previous destination: "$percent >> ../run/$num/output.txt

cd  ../run/$num/
tar czf  counts.txt.tgz counts.txt && rm counts.txt
cd - &> /dev/null

#echo "Processing $num Done." | mail -s "VDiSC" anaskatib@mail.umkc.edu

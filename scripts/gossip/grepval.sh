#!/bin/bash

echo 'Ave Read & Count Time:'
for runnum in "$@"
do
 nm=$(grep 'Ave Read & Count Time' $runnum/output.txt | cut -d: -f2)
 echo  $runnum': '$nm
done


echo 'Number of DiSC messages:'
for runnum in "$@"
do
 nm=$(grep 'Number of DiSC messages' $runnum/output.txt | cut -d: -f2)
 echo  $runnum': '$nm
done

echo ''
echo 'Ave size of DiSC messages:'
for runnum in "$@"
do
 as=$(grep 'Ave size of DiSC messages' $runnum/output.txt | cut -d: -f2)
 echo $runnum': '$as
done

echo ''

echo 'Ave Size Reduction:'
for runnum in "$@"
do
 as=$(grep 'Ave Size Reduction:' $runnum/output.txt | cut -d: -f2)        
 echo $runnum': '$as
done

echo ''
echo 'Packet Loss Rate:'
for runnum in "$@"
do
 pl=$(grep 'Packet Loss Rate:' $runnum/output.txt | cut -d: -f2)       
 echo $runnum': '$pl
done

echo ''
echo 'Delay Const:'
for runnum in "$@"
do
 dc=$(grep 'delay_const=' $runnum/config.txt | cut -d= -f2 )
 echo $runnum': '$dc
done

echo ''


echo 'First Node:'
for runnum in "$@"
do
 fn=$(grep 'First Node  Node (' $runnum/output.txt | rev | cut -d' ' -f1 | rev)
 echo  $runnum': '$fn
done

echo ''

echo 'Last Node:'
for runnum in "$@"
do
 fn=$(grep 'Last  Node  Node (' $runnum/output.txt | rev | cut -d' ' -f1 | rev)
 echo  $runnum': '$fn
done

echo ''


echo ''
echo 'Stop Time:'
for runnum in "$@"
do
 st=$(grep 'StopTime' $runnum/config.txt | cut -d'=' -f2 ) 
 echo $runnum': '$st
done


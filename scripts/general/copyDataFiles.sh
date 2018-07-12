#!/bin/bash

file=$1

for i in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15; 
do
    echo 'copying '$i
    f=$((i+1))
	scp $file anask@cp-"$i":"$file.$f-0"
done



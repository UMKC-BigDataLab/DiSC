#!/bin/bash 
#
# 1-> rawCounts 2-> sorted, one lined
if [ "$#" -ne 2 ]; then
	echo "Usage: $0 rawCounts.txt sortedCounts.txt"
	exit 1
fi

cat $1 | sed -z $'s/:\\\n/ /g' | sed -z $'s/]\\\n\[/] [/g' | sort > $2

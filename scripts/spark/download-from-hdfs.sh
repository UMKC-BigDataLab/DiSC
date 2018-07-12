#!/bin/bash 
#
# 1-> rawCounts 2-> sorted, one lined
if [ "$#" -ne 2 ]; then
	echo "Usage: $0 <hdfs_file_name>  <lcl_file_name>"
	exit 1
fi

hdfs dfs -cat $1 > tmp
./sort-counts.sh tmp $2
rm tmp

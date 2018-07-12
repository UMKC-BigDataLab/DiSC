#!/bin/bash
#
# usage:
# ./runClusterCMD.sh <num_slaves> <cmd>
# example:
# ./runClusterCMD.sh 15 'cd target && taskset -c 05 java -Xmx50g -jar gossip-0.1.0-incubating-jar-with-dependencies.jar &> ~/test.txt' 
# 

if [ $# != 2 ]; then
        echo "$0 <num_slaves> <cmd>"
        exit
fi 

SLVS=$1
cmd=$2
echo -n "EXECUTING CLUSTER COMMAND ON $((SLVS+1)) NODES.."
eval $cmd &
start=$(date +%s.%N)
#echo '    Node-1'
for s in `seq $SLVS`; do
	#echo '    Node-'$((s+1))
	ssh -q anask@cp-$s "$cmd  &" &
done
wait

dur=$(echo "$(date +%s.%N) - $start" | bc)

printf "DONE. [%.2fs]\n" $dur
exit 0

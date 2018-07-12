#!/bin/bash
#
# ./cpier.sh file destDir numNodes
if [ $# != 3 ]; then
        echo "$0 <file> <destDir> <numSlaveNodes>"
        exit
fi

echo -n "COPYING $1 TO $2 ON $3 CLUSTER SLAVES.."
for i in `seq $3`
do
        #echo 'COPYING TO '$i
        scp -q -r $1 anask@cp-$i:$2
done
echo "DONE."

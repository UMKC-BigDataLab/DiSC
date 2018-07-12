#!/bin/bash
# Copy logs from remote nodes to one local file.
# All logs should have the same name with a .txt extenstion.
# filename format: filename.txt
#./logger.sh <number_of_slaves> <file(s)_to_log>

if [ $# -lt 2 ]; then
        echo "$0 <number_of_slaves> <file(s)_to_log>"
        exit
fi
 
start=$(date +%s.%N)
for file in "${@:2}"
do

        log=$(basename $file .txt).log
        echo 'LOGGING: '$log
        cp ~/$file /dev/data/$log
        for s in `seq $1`
        do
                scp -q anask@cp-$s:~/$file tmp.txt
                cat tmp.txt >> /dev/data/$log
        done
		rm tmp.txt
		#echo "ZIPPING.."
        #cd /dev/data
		#tar czf $log".tgz" $log
		#rm $log
        #cd -
done
dur=$(echo "$(date +%s.%N) - $start" | bc)
printf "DONE LOGGING. [%.2fs]\n" $dur


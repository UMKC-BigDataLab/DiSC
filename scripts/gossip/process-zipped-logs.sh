#!/bin/bash
# Process DiSC combined log file.
start=$(date +%s.%N)
for log in  "$@"                                                               
do                                                                        
        exp_num=$(date +"%Y-%m-%d.%H-%M")
        fpath=$(dirname "${log}")
        #echo 'FILE STORED IN: '$fpath
        #echo "UNZIPPING.."
        #cd $fpath                                 
        #tar xzf $log".tgz"
        #cd -

        echo "PROCESSING: "$log
        ./runall-procssing.sh $log $exp_num
        #rm $fpath$log
done
dur=$(echo "$(date +%s.%N) - $start" | bc)
printf "DONE PROCESSING. [%.2fs]\n" $dur
                                                            

#echo "All Processing Done for $exp_num" | mail -s "DiSC" anaskatib@mail.umkc.edu 

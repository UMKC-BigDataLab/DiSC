#!/usr/bin/env bash

EXPNAME=higgs
NODS=16
FPATH=/dev/data
FAMS=$FPATH/higgs-families.txt
VARS=$FPATH/higgs-variables.txt
TABL=$FPATH/higgs.table.txt

SLVS=$((NODS-1))
LR40=100
LR80=200
LR120=300

printf "GENERATING CONFIG FILES.."
cd ../gossip 
./create-config.sh -n $NODS -k 1 -f $FAMS -v $VARS -t $TABL &> /dev/null
./create-config.sh -n $NODS -k 2 -f $FAMS -v $VARS -t $TABL &> /dev/null
./create-config.sh -n $NODS -k 3 -f $FAMS -v $VARS -t $TABL &> /dev/null
cd ../general
printf "DONE.\n"

echo "RUNNING EXPERIMENT: $EXPNAME"
startE=$(date +%s.%N)
for R in 40 80 120; do
  for K in 1 2 3; do
    startK=$(date +%s.%N)
    echo ""
    echo "RUNNING R=$R K=$K"
    cd ../general
    cp ../gossip/config.k$K.txt CONFIG
    cp CONFIG ~/target/configuration/config.txt
    sed -i 's/P111/80/g' ~/target/configuration/config.txt
    sed -i 's/P222/40/g' ~/target/configuration/config.txt
    sed -i "s/RRR/$R/g" ~/target/configuration/config.txt

    if   [  "$R" == "40"  ]; then                                                   
        sed -i 's/LLL/'$LR40'/g'  ~/target/configuration/config.txt
    elif [  "$R" == "80"  ]; then                                                   
        sed -i 's/LLL/'$LR80'/g'  ~/target/configuration/config.txt
    elif [  "$R" == "120" ]; then                                                   
        sed -i 's/LLL/'$LR120'/g' ~/target/configuration/config.txt
    else
        sed -i 's/LLL/300/g'      ~/target/configuration/config.txt
    fi   

    sed -i 's/DPF/1.8/g'   ~/target/configuration/config.txt
    sed -i 's/IES/135000/g' ~/target/configuration/config.txt
    
    ./cpier.sh ~/target/ ~/ $SLVS
    cp ~/target/configuration/config.txt ../gossip/
    cp ~/target/configuration/config.txt $FPATH/$EXPNAME.r$R.k$K.config 

    ./runClusterCMD.sh $SLVS 'cd ~/target && taskset -c 00,01,02,03,04,05,06,07 java -Xmx50g -jar gossip-0.1.0-incubating-jar-with-dependencies.jar &> ~/'$EXPNAME'.r'$R'.k'$K'.txt'
    
    sleep 10
    
    ./logger.sh $SLVS $EXPNAME.r$R.k$K.txt
    
    cd ../gossip &> /dev/null
    ./process-zipped-logs.sh  $FPATH/$EXPNAME.r$R.k$K.log
    durK=$(echo "$(date +%s.%N) - $startK" | bc)
    printf "DONE RUNNING R=$R K=$K. [%.2fs]\n" $durK

  done;
done;

durE=$(echo "$(date +%s.%N) - $startE" | bc)
printf "DONE RUNNING EXPERIMENT $EXPNAME. [%.2fs]\n" $durE


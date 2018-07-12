#!/bin/bash

exit 0
mkdir -p run-times
MYMASTER="spark://ctl.m400.nosql-json-PG0.utah.cloudlab.us:7077"

for p in 0.001 0.005 0.01 0.02 0.04 0.06 0.08 0.10 100; do
    for t in HIGGS tweets syn1 syn2 syn3 syn4; do
        echo "Running "$t" "$p

        variables=/dev/data/syn-variables.txt
        families=/input/syn-families.txt
        num_variables=100
        if [[ $t == "higgs" ]]; then
             variables=/dev/data/higgs-variables.txt
             families=/input/higgs-families.txt
             num_variables=29

        elif [[ $t == "HIGGS" ]]; then
             variables=/dev/data/HIGGS-variables.txt
             families=/input/HIGGS-families.txt
             num_variables=240

        elif [[ $t == "tweets" ]]; then
             variables=/dev/data/twtr-variables.txt
             families=/input/twtr-families.txt
             num_variables=136

        fi

        # copy template and rename input/output files
        cat configuration/scMapReduce.conf.tmp | sed -e "s#TABLENAME#$t.table.txt.$p#g; s#VARS#$variables#g;  s#FAMS#$families#g; s#NUM_VS#$num_variables#g; s#SPARK_MASTR#$MYMASTER#g"  > configuration/scMapReduce.conf

        # run spark
        $SPARK_HOME/bin/spark-submit  --class edu.umkc.sce.csee.dbis.Main  sc.mapreduce-0.0.1-SNAPSHOT-jar-with-dependencies.jar  --executor-cores 8 --deploy-mode local &> run-times/$t.$p.timing.txt
    sleep 30
    done;
done;


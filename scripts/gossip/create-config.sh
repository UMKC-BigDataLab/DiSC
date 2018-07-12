#!/bin/bash
#==============================================================================
#
#          FILE:  create-config.sh
#
#         USAGE:  ./create-config.sh -n <number-of-nodes> -k <lsh-k> -f <families-file>
#
#   DESCRIPTION:  This script is used to generate the configuration file for 
#                 gossiping. It assumes global communication, master as "ctl"
#                 and all slaves "cp-x".
#
#       WARNING:  ---
#       OPTIONS:  -n   number of nodes in the cluster
#                 -k   how many nodes are responsible for each family
#                 -f   file that contains all the families to be gossiped
#
#  REQUIREMENTS:  ---
#          BUGS:  ---
#         NOTES:  ---

#        AUTHOR:  Anas Katib, anaskatib@mail.umkc.edu
#   INSTITUTION:  University of Missouri-Kansas City
#       VERSION:  1.0
#       CREATED:  10/04/2017 11:00:00 AM CST
#      REVISION:  ---
#
#==============================================================================

scriptname=$0
MASTR="ctl"
SLVPREFX="cp-"

function usage {
    echo "USAGE: $scriptname -n <num-nodes> -k <lsh-k> -f <families-file> -v <vars-file> -t <table-file>"
    echo "  -n <num-nodes>      number of nodes in the cluster (including the master)"
    echo "  -k <lsh-k>          number of nodes that are assigned the same family"
    echo "  -f <families-file>  file that contains all the families to be gossiped"
    echo "  -v <vars-file>      file that contains the variables that were used to generate the families"
    echo "  -t <table-file>     input table used to calculate the family counters"
    echo "  -h                  print this message"
    exit 1
}

function aparse {
while [[ $# > 0 ]] ; do
  case "$1" in
    -n)
      NNODES=${2}
      shift
      ;;
    -k)
      LSHK=${2}
      ;;
    -f)
      FAMS=${2}
      ;;
    -v)
      VARS=${2}
      ;;
    -t)
      TBL=${2}
      ;;
 esac
  shift
done
}

# check if proper input is entered
if [[ ($# -eq 0) || ( $@ == *"-h") || ( $1 != "-n" ) || ( $3 != "-k" ) || $5 != "-f"  || $7 != "-v" || $9 != "-t" ]] ; then
    usage
    exit 1
fi

aparse "$@"
set -e

echo -e "\nSCRIPT STARTED..\n"
echo -e "RUNNING WITH:\n    Number of nodes:" $NNODES "\n    LSH-K:" $LSHK "\n    Families:" $FAMS
echo -e "    Variables: "$VARS "\n    Table: "$TBL "\n"

echo "GETTING IP ADDRESSES.."
# get ips of all nodes in the cluster

#     master:
IP=$(ssh ctl '(hostname --ip-address)')
echo $IP > ips.txt

#     slaves
for (( n=1; n<$NNODES; n++ )) ; do
	IP=$(ssh cp-"$n" '(hostname --ip-address)')
	echo $IP >> ips.txt
done

echo "Generating default configuration arguments.."
echo "P1=P111
P2=P222
family_file=$FAMS
variables_file=$VARS
table_file=$TBL
K=5
R=RRR
EstLogLimit=LLL
delay_const=10
PackingFactor=DPF
ExtractionSize=IES
AlgorithmType=Sum
ReadStoredVals=False
ReadMembershipList=True
LocalMmbrCount=0
BlockSize=75
StopTime=1020
CompressDiscData=True
NumberOfTableSplits=8
GossipAllFamilies=False
CreateSplits=True
" > config.k$LSHK.txt

echo "GENERATING DESTINATION ADDRESSES"
python ../ips/ips.global.py ips.txt >> config.k$LSHK.txt

echo "ASSIGNING FAMILIES.."
python ../families/familier-lsh-ch.py $FAMS 50 $LSHK $NNODES >> config.k$LSHK.txt

rm ips.txt
echo "SCRIPT FINISHED."
exit 0

#!/bin/bash
#
# Usage   ./getIps.sh <addresses_file> <private_key> 
# Example ./getIps.sh addresses.txt /Users/anask/.ssh/cloud_lab 

usrnm="anask"
input=$1
key=$2
while IFS= read -r address
do
  ssh -o "StrictHostKeyChecking no" -i $key  $usrnm@$address "hostname --ip-address" < /dev/null
done < "$input"


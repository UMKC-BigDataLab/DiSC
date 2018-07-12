#/bin/bash

ssh cp-$1 'hostname > ~/hostname.txt' 

ssh cp-$1 "cat /etc/hosts > ~/hosts.txt
     grep -i -f ~/hostname.txt /etc/hosts | sort -n | sed '1d' | xargs -I X sed -e s/X//g -i ~/hosts.txt
     sudo cp ~/hosts.txt /etc/hosts
"

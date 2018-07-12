#!/usr/bin/env python
# ./script ips.txt
import sys

def main(argv):
    f = open(argv[0],"r")
    ips = [ip.rstrip('\n') for ip in f]
    f.close()
    # create ip list
    ip_str = ""
    ip_membrs = ""
    port = "2000*"
    id = 1
    numPeers=8
    for ip in ips:
        ip_str += ip+";"
        ip_membrs += ip+":"+port+","+str(id)+"-*;"
        id += 1
    print("IPS="+ip_str)
    for i in range(len(ips)):
        for p in range(numPeers):
            print("Node_"+str(i+1)+"-"+str(p)+"_mmbrs="+ip_membrs.replace("*",str(p)))



if __name__ == "__main__":
    main(sys.argv[1:])

#!/usr/bin/env python
import sys

def main(argv):
    f = open(argv[0],"r")
    ips = [ip.rstrip('\n') for ip in f]
    f.close()
    # create ip list
    ip_str = "IPS="
    ip_membrs = ""
    port = 20000
    id = 0
    numPeers=8
    for ip in ips:
        id += 1
        ip_str += ip+";"
        p_str = ''
        for p in range(numPeers):
        	p_str += ip + ':' + str(port+p) + ','+str(id)+'-'+str(p)+';'
        for p in range(numPeers):
        	print("Node_"+str(id)+"-"+str(p)+"_mmbrs="+p_str)

    print(ip_str)
if __name__ == "__main__":
    main(sys.argv[1:])

#!/usr/bin/env python
import sys

def main(argv):
    f = open(argv[0],"r")
    ips = [ip.rstrip('\n') for ip in f]
    f.close()
    # create ip list
    ip_str = ""
    ip_membrs = ""
    port = "20000"
    id = 1
    for ip in ips:
        ip_str += ip+";"
        ip_membrs += ip+":"+port+","+str(id)+"-0;"
        id += 1
    print("IPS="+ip_str)
    IDS = "IDS="
    for i in range(len(ips)):
        print("Node_"+str(i+1)+"-0_mmbrs="+ip_membrs)
        if len(IDS) == 4:
            IDS += ip_membrs
    print(IDS)

if __name__ == "__main__":
    main(sys.argv[1:])

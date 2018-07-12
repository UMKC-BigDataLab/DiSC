#!/usr/bin/python

import sys

def main(args):
    fin = open(args[0],"r")
    comp = 0.0
    comp_len = 0.0

    decomp = 0.0
    decomp_len = 0.0

    save = 0.0
    save_len = 0.0
    """
    INFO 11:14:55.106 MsCompressTime=22
    INFO 11:14:55.116 MsDecompressTime=5
    INFO 11:14:55.132 %SizeReduction=11.49016
    """
    for line in fin:
        if "MsCompressTime" in line:
            dur = float(line.split("=")[1].strip())
            comp += dur
            comp_len += 1
        elif "MsDecompressTime" in line:
            dur = float(line.split("=")[1].strip())
            decomp+= dur
            decomp_len += 1

        elif "%Size" in line:
            prcnt = float(line.split("=")[1].strip())
            save += prcnt
            save_len += 1

    print("Ave Results for "+args[0])
    print("\t          Comp Time: "+ str(comp/comp_len) +" ms")
    print("\t        Decomp Time: "+ str(decomp/decomp_len)+" ms")
    print("\tAggregate CoDe Time: "+ str((comp+decomp)/(comp_len+decomp_len))+" ms")
    print("\t        Space Saved: "+ str(save/save_len)+ " %")

if __name__ == "__main__":
    main(sys.argv[1:])
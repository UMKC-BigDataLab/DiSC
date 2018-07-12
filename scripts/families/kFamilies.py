#!/usr/bin/python

# ./script families.txt num_to_print mxFamSize

import sys

def main(argv):
    l=0
    mxNum = int(argv[1])
    mxSize = int(argv[2])
    for line in open(argv[0]):
        l += 1
        lineAr = line.strip().split(",")
        if len(lineAr) <= mxSize:
            print line.strip()
        if l >= mxNum:
            return

if __name__ == "__main__":
    main(sys.argv[1:])




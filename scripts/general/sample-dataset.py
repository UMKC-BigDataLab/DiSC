#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Usage: ./sample-dataset.py -h
# Note: to make this script executable: sudo chmod +x simple.py
# Anas Katib

import sys
import time
from random import randint

def main(args):
    filename = args[0]
    n = int(args[1])
    k = int(float(args[1])*(float(args[2])))

    if (k < 1):
        sys.stderr.write("Error: dataset size or sample percentage is too ")
        sys.stderr.write("small!\n")
        quit()

    # generate a set of k random numbers from 1 to n
    lines = set()
    i = 0
    while i < k:
        lines.add(randint(1,n))
        i = i + 1

    while len(lines) < k:
        lines.add(randint(1,n))

    # print the selected lines
    selected = list(sorted(lines))
    i = 0
    f = 1
    with open(filename, 'r') as inputfile:
        while i < k:
            line = inputfile.readline().rstrip()
            if len(line) < 1:
                sys.stderr.write("Error: line number ["+str(f)+"] is empty or ")
                sys.stderr.write("does not exist!\nEXITING..\n")
                quit()

            if f == selected[i]:
                 print(line)
                 i = i + 1

            f = f + 1

if __name__ == "__main__":
    args = sys.argv

    if len(args) != 4:
        print("\nUsage: "+args[0]+" <input_file> <input_file_num_lines> <sample_precentage>")
        print("\nExample: "+args[0]+" large.csv 200000000 0.10")
        print("\nDescription: return a random sample from the input file. The sample")
        print("size is determined by the supplied percentage.")
        quit()

    main(sys.argv[1:])

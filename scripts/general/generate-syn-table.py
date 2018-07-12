#!/usr/bin/env python
#
# Anas Katib
# Dec 14, 2017
#
from random import randint
import sys
from multiprocessing import Process
import fileinput
import os
import time

def create_part(filename, n, p, m, num_cols, cols_p):
    part_file = open(filename, "w")
    use_weighted_cols = False
    if (len(cols_p) > 0):
        num_cols = len(cols_p)
        use_weighted_cols = True

    lines = [] # store the lines to be printed 
    L = 0; # print every m lines use L to track it
    
    while n > 0:
        c = 0
        line = []
        while c < num_cols:
             if (use_weighted_cols is False):
                 if randint(0, 100) < p:
                     line.append ('1')
                 else:
                     line.append('0')
             else:
                 if randint(0, 100) < cols_p[c]:
                     line.append ('1')
                 else:
                     line.append('0')
             c = c + 1

        lines.append(",".join(line))
        n = n - 1
        L = L + 1
        if L > m or n == 0:
            L = 0
            part_file.write("\n".join(lines)+"\n")
            lines = []
    part_file.close()
    

def main(args):
    filename = args[0] 
    R = int (args[1]) # number of rows to generate
    C = int (args[2]) # number of columns to generate 
    P = int (args[3]) # use probabilty P for all cols
    M = int (args[4]) # print every M lines generated

    # use different weights/probabilty for every column?
    WP = False
    w = []
    if len(args) > 5:
        WP = True if args[5].lower() in ["true", "t", "yes", "y"] else False

    if WP is True: 
        for i in range(C):
            w.append(randint(0, 100))
        sys.stderr.write("Using weights: "+ str(w)+"\n")

    threads = 40 
    n = R/threads
    r = R % threads

    processes = []
    files = []

    for t in range(threads - 1):
       processes.append (Process(target=create_part, args=(filename+"_"+str(t), n, P, M, C, w)))
       files.append(filename+"_"+str(t))

    processes.append(Process(target=create_part, args=(filename+"_"+str(threads - 1), n+r, P, M, C, w)))
    files.append(filename+"_"+str(threads-1))

    start = time.time()

    for t in processes:
        t.start()   

    for t in processes:
        t.join()   

    end = time.time()
    #print(end - start)

    # merge created parts
    outfile = files[0]
    files.pop(0)

    with open(outfile, 'a') as fout:
      fin = fileinput.input(files)
      for line in fin:
        fout.write(line)
      fin.close()

    for f in files:
         os.remove(f) 

    os.rename(outfile, filename)

    start = time.time()
    #print(start - end)

if __name__== "__main__":
    if  len(sys.argv[1:]) < 5 or sys.argv[1] in ["-h","--help","-help"]:
        print("\nUsage: "+sys.argv[0]+" <table_name> <rows> <cols> <p> <print_block> [use_multiple_p]")
        print("\nExample: "+sys.argv[0]+" table.csv 100 10 25 20 False")
        print("\nDescription: generate a csv table of binary numbers.\n")
        quit()
 
    main(sys.argv[1:])

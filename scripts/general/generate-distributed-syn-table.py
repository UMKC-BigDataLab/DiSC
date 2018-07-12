#!/usr/bin/env python
#
# Anas Katib
#
from random import random, randint, uniform
import sys
from multiprocessing import Process, Value, Lock
import fileinput
import os
import time
import datetime
import numpy.random as nrand
from  numpy import sort, append
from scipy.stats import binom

class DistributionType:
    OTHER, BINOMIAL = range(2)


def bin_search(A, start, end, val):
    mid = (start + end)/2

    if start > end:
        return mid+1

    if A[mid] == val:
       return mid

    if A[mid] >= val:
        return bin_search(A, start, mid-1, val)
    else:
        return bin_search(A, mid+1, end, val)


def create_section(means, start, width):
    section = ['0'] * width
    col_prob =  random()
    m = bin_search(means, start, start+width-2, col_prob)
    section[m-start] = '1'
    return ",".join(section)


def create_row(means, widths):
    row = []
    c = 0
    for width in widths:
       row.append(create_section(means, c, width))
       c = c + width
    #sys.stderr.write(" ".join(row)+"\n")
    return ",".join(row)


def create_part(filename, num_rows, means, section_widths, buffer_size,
               counter, lock):
    part_file = open(filename, "w")
    rows_buffer=[]
    bsize = 0
    for r in range(num_rows):
        rows_buffer.append(create_row(means, section_widths))
        bsize += 1
        if bsize == buffer_size or r == num_rows - 1:
            part_file.write("\n".join(rows_buffer))
            part_file.write("\n")
            with lock:
                counter.value += bsize
            bsize = 0
            rows_buffer = []
    part_file.close()


def track_status(interval, goal, counter, lock):
    while True:
        time.sleep(interval)
        with lock:
            sys.stderr.write("    Number of lines remaining: {:,}\r".format(goal - counter.value))


def distribute(distro, min_span, max_span, distro_type, p):
    num_cols = len(distro)
    assigned_count = 0
    section_width = []
    while assigned_count < num_cols:
        # pick a random number of columns that belong to one distribution
        # such that the number is < span
        distro_width = randint(min_span, max_span)
        if num_cols - assigned_count < distro_width:
            distro_width = num_cols - assigned_count
        section_width.append(distro_width)
        # pick a random mean
        mean = random()
        section_probs = []
        if DistributionType.BINOMIAL:
            total = 0
            if p is None:
                p = random()
            for num_heads in range(distro_width):
                px = binom.pmf(num_heads, distro_width - 1, p)
                section_probs.append(px+total)
                total += px
            print(section_probs)


        for c in xrange(distro_width):
            if assigned_count < num_cols:
                if DistributionType.BINOMIAL:
                    distro[assigned_count] = section_probs[c]
                else:
                    d = num_cols - assigned_count
                    if d > 1:
                        d = 1 / d
                    distro[assigned_count] = mean * d
                assigned_count += 1
                mean = (mean/2)

    return section_width


def main(args):
    filename = args[0]
    R   = int (args[1]) # number of rows to generate
    C   = int (args[2]) # number of columns to generate
    Smn = int (args[3]) # min distribution columns span
    Smx = int (args[4]) # max distribution columns span
    B = 50              # print every B lines generated
    F = None            # fairness of binomial coin

    if len(args) > 5:
        input_str = " ".join(args[5:])
        valid_B = False
        try:
            B = int (args[5])
            valid_B = True
        except ValueError:
            pass

        try:
            if not valid_B:
                input_str = args[5]
                F = float(args[5])
            elif len(args) > 6:
                input_str = args[6]
                F = float(args[6])

            if F is not None and F < 0 or F > 1:
                raise ValueError()

        except ValueError:
                sys.stderr.write("Error: invalid input '"+input_str+"'\n")
                sys.exit(1)

    if Smn > Smx or Smn < 1 or Smx < 1:
        sys.stderr.write("Error: invalid min/max spans.\n")
        sys.exit(1)

    # create C columns to store means
    means = [0] * C

    # assign a normally distributed mean for sections of columns
    section_widths = distribute(means, Smn, Smx, DistributionType.BINOMIAL, F)
    sys.stderr.write("Levels:  "+str(section_widths)+"\n")

    # print column names
    sys.stderr.write("Columns: [")
    v = 1
    section_col_names = []
    for width in section_widths:
        for c in xrange(width):
            section_col_names.append("col_{var}.{col}".format(
                var=v, col=c+1))
        v += 1
    sys.stderr.write(", ".join(section_col_names)+"]\n")

    # print the distributed means
    sys.stderr.write("Means:   \n")
    m = 0
    for section, width in enumerate(section_widths):
        sys.stderr.write("      ("+str(section+1)+") [ ")
        for mean in range(width):
            sys.stderr.write(str(means[m])+" ")
            m += 1
        sys.stderr.write("]\n")

    # generate table
    threads = 40
    num_rows = R/threads
    remaning_rows = R % threads
    processes = []
    files = []
    line_count =  Value('i', 0)
    lock = Lock()
    for t in range(threads - 1):
       processes.append (Process(target=create_part, args=(
           filename+"_"+str(t),
           num_rows,
           means,
           section_widths,
           B, line_count, lock)))
       files.append(filename+"_"+str(t))

    processes.append (Process(target=create_part, args=(
        filename+"_"+str(threads-1),
        num_rows + remaning_rows,
        means,
        section_widths,
        B, line_count, lock)))

    tracker = Process(target=track_status, args=(4, R, line_count, lock))

    files.append(filename+"_"+str(threads-1))

    start = time.time()

    for t in processes:
        t.start()

    tracker.start()
    for t in processes:
        t.join()

    sys.stderr.write("\rMerging..                                           \r")
    tracker.terminate()

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

    end = time.time()

    sys.stderr.write("Generated a {} x {} table in {}\n".format(
        R, C, datetime.timedelta(seconds=end - start)))

if __name__== "__main__":
    if  len(sys.argv[1:]) < 5 or sys.argv[1] in ["-h","--help","-help"]:
        print("\nUsage: "+sys.argv[0]+(" <table_name> <rows> <cols> "
                                       "<min_span> <max_span> "
                                       "[buffer_size] [fairness]" ))

        print("\nExample: "+sys.argv[0]+" table.csv 100 10 1 4 50 0.5")
        print(("\nDescription: generate a csv table of noramlly "
               "distributed binary numbers.\n"))
        print("Details:\n"
              "    table_name      file name in which to store the output.\n"
              "    rows            number of rows in the table.\n"
              "    cols            number of columns in the table.\n"
              "    min_span        min number of columns that belong to a\n"
              "                    distribution with a randomly chosen mean.\n"
              "    max_span        similar to min_span.\n"
              "    buffer_size     number of lines (integer) to store in\n"
              "                    memory before printing or flushing.\n"
              "                    (default: 50).\n"
              "    fairness        a floating point number to indicate the\n"
              "                    fairness of the bionomial distribution:\n"
              "                    a value [0,1] (default: mixed fairness).\n"
              )


        quit()
    sys.stderr.write("Running: "+" ".join(sys.argv)+"\n")
    main(sys.argv[1:])

#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Anas Katib
# Feb 26, 2018
#
# Usage: ./extract-family-relerr.py -h

import argparse
import sys
import time
import json
import ast

def eprint(obj):
  sys.stderr.write(obj)
  sys.stderr.write('\n')

def main(args):

  filename = args.counts
  eprint("Reading: "+filename)

  sTime = time.time()

  reachedTrue = 0
  reachedEstErr = 0
  reachedRelErr = 0

  trueCounter = {}
  estimatedCounter = {}
  relErrCounter = {}

  estimationCounts = {}
  relErrCounts = {}

  # open input file and read it
  countFile = open(filename,'r')
  for line in countFile:

    if reachedTrue == 0 and line.strip()  == "TRUE:" :
      reachedTrue = 1

    if reachedEstErr == 0 and line.strip()  == "ESTIMATED:" :
      reachedEstErr = 1

    if reachedRelErr == 0 and line.strip()  == "RELATIVE ERROR:" :
      reachedRelErr = 1
      reachedEstErr = 2
      print ("OK")

    # read true counters
    if reachedTrue == 1 and line.strip()  != "TRUE:": # next line
      trueStr = ast.literal_eval(line)
      trueCounterObj = json.loads(json.dumps(trueStr)) # read true counter

      for family in trueCounterObj.keys():
        trueCounter[family] = trueCounterObj[family]['0']
      reachedTrue = 2

    # read estimated errors
    if reachedEstErr == 1 and line.startswith("Node:"):
      # Node: 14-0 asgwear,agqr,anklemonitor4HRC,aocevents [[..]]
      counterIndex = line.index("[")
      counter = ast.literal_eval(line[counterIndex:])
      familyName = line[0:counterIndex].split()[2]

      if 'fr-CA,da,lb,pl' in familyName:
        print(counter)
      if familyName not in estimatedCounter:
        estimatedCounter[familyName] = counter
        estimationCounts[familyName] = 1

      else:
        estimatedCounter[familyName] = map(lambda x,y: x+y, estimatedCounter[familyName],counter)
        estimationCounts[familyName] += 1




    # read relative errors
    if reachedRelErr == 1 and line.startswith("Node:"):
      # Node: 14-0 asgwear,agqr,anklemonitor4HRC,aocevents [[..]]
      counterIndex = line.index("[")
      counter = ast.literal_eval(line[counterIndex:])
      familyName = line[0:counterIndex].split()[2]
      if 'fr-CA,da,lb,pl' in familyName:
        print(counter)
      if familyName not in relErrCounter:
        relErrCounter[familyName] = counter
        relErrCounts[familyName] = 1

      else:
        relErrCounter[familyName] = map(lambda x,y: x+y, relErrCounter[familyName],counter)
        relErrCounts[familyName] += 1

  #for family in estimatedCounter.keys():
  #  estimatedCounter[family] = [[int(cell/estimationCounts[family]) for cell in row] for row in estimatedCounter[family]]

  #print(relErrCounter[family])

  #for family in relErrCounter.keys():
  #  relErrCounter[family] = [[cell/relErrCounts[family] for cell in row] for row in relErrCounter[family]]

  #print(relErrCounter[family])
  print("TRUE")
  print(trueCounter['fr-CA,da,lb,pl' ])
  eTime = time.time()


  eprint(str(eTime - sTime) + 's')
  eprint("Done.")

if __name__ == "__main__":
  prog_desc = "Read true counters and per node familiy estimated counters "\
    "and relative error then print out the average realtive error and estimated "\
    "counter per family and the true counter."


  parser = argparse.ArgumentParser(description = prog_desc, add_help=False)
  parser.add_argument_group('required arguments')

  # Add a required argument
  parser.add_argument('counts', help='counts.txt uncompressed counts file.',
    metavar=('<counts_file>'), type=str)

  # Help argument
  parser.add_argument('-h','--help',action='help',default=argparse.SUPPRESS,
    help=argparse._('show this help message and exit.'))

  # Rename the arguements' title. The default (i.e. positional arguments)
  # is a bit confusing to users.
  parser._positionals.title = "required arguments"

  args = parser.parse_args()
  main(args)
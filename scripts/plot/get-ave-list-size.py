#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Anas Katib
# May 19, 2017
#
# Usage: ./get-ave-list-size.py -h

import argparse
import sys
import time

def eprint(obj):
  sys.stderr.write(obj)
  sys.stderr.write('\n')

def main(args):
  sTime = time.time()

  files = args.sFile
  aves = {}
  for filename in files:
    statsFile = open(filename,'r')
    nodes = {}
    for line in statsFile:
      node = int(line.split("(")[1].split(")")[0].split('-')[0].strip())

      listSize = line.split("Total:")[1].strip()
      listSize = int(listSize.split(",")[0])

      if node not in nodes:
        nodes[node]=[]
      if node not in aves:
        aves[node]= str(node)
      nodes[node].append(listSize)

    for key in sorted(nodes.iterkeys()):
      avg = sum(nodes[key]) / float(len(nodes[key]))
      aves[key] = aves[key] + "\t" + "{0:.2f}".format(avg)

  for key in sorted(nodes.iterkeys()):
    print "%s" % (aves[key])

  eTime = time.time()

  #eprint("Done in "+str(eTime - sTime) + 's' )

if __name__ == "__main__":
  prog_desc = "The program reads DiSC visible families statistics file(s) and computes the avearge "\
    "list size (i.e. the number of families) for all nodes."\


  parser = argparse.ArgumentParser(description = prog_desc, add_help=False)
  parser.add_argument_group('required arguments')

  # Add a required argument
  parser.add_argument('sFile',nargs='+', help='visible-families-stats file(s)',
    metavar=('<stats_file>'), type=str)


  # Help argument
  parser.add_argument('-h','--help',action='help',default=argparse.SUPPRESS,
    help=argparse._('show this help message and exit.'))

  # Rename the arguements' title. The default (i.e. positional arguments)
  # is a bit confusing to users.
  parser._positionals.title = "required arguments"

  args = parser.parse_args()
  main(args)


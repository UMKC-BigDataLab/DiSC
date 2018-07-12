#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Anas Katib
# May 19, 2017
#
# Usage: ./parse-relative-err.py -h

import argparse
import sys
import time
import os

def eprint(obj):
  sys.stderr.write(obj)
  sys.stderr.write('\n')

def main(args):
  filename = args.statsFile
  output = args.outputDir
  if not os.path.exists(output):
    os.makedirs(output)

  sTime = time.time()
  sfile = open(filename, 'r')
  n = 0
  for line in sfile:
    if 'Node' in line:
      n += 1
      if n>1:
        outf.close()
      outf = open(output+'/'+str(n)+'.txt', 'w')
      outf.write('"Node '+line[:-3].split()[1].split('-')[0][1:]+'"\n')
    elif line[0].isdigit():
      stats = line.split()
      if '.' not in stats[0]:
        outf.write(stats[0].split(':',1)[1])
      else:
        outf.write(stats[0].split(':',1)[1].split('.')[0])
      outf.write('\t'+stats[1]+'\n')
  sfile.close()




  eTime = time.time()


  #eprint(str(eTime - sTime) + 's')
  #eprint("Done.")

if __name__ == "__main__":
  prog_desc = "Parse the relative error statistics file and extract the mean realtive error"\
              "for every node"

  parser = argparse.ArgumentParser(description = prog_desc, add_help=False)
  parser.add_argument_group('required arguments')

  # Add a required argument
  parser.add_argument('statsFile', help='statsFile contains statistics data.',
    metavar=('statsFile'), type=str)

  # Add a required argument
  parser.add_argument('outputDir', help='outputDir directory to save output files.',
    metavar=('outputDir'), type=str)


  # Help argument
  parser.add_argument('-h','--help',action='help',default=argparse.SUPPRESS,
    help=argparse._('show this help message and exit.'))

  # Rename the arguements' title. The default (i.e. positional arguments)
  # is a bit confusing to users.
  parser._positionals.title = "required arguments"

  args = parser.parse_args()
  main(args)

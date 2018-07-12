#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Anas Katib
# May 23, 2017
#
# Usage: ./cal-lost-packets.py -h

import argparse
import sys
import time


def eprint(obj):
    sys.stderr.write(obj)
    sys.stderr.write('\n')

def main(args):
  filename = args.logfile
  sentCount = 0.0
  rcvdCount = 0.0

  sTime = time.time()
  logfile = open(filename,'r')
  for line in logfile:
    if 'received DiSC' in line:
      rcvdCount += 1
    elif 'DiSC Data Sending' in line:
      sentCount += 1
  eTime = time.time()
  logfile.close()

  ratio = round(100*(1 - (rcvdCount/sentCount)),2)
  print("Number of packtes sent: "+str(int(sentCount)) )
  print("Number of packtes received: "+str(int(rcvdCount)) )
  print("Packet Loss Rate: " + str(ratio) +"%")
  #eprint("Log processed in "+str(round(eTime - sTime,3)) + 's')
  #eprint("Done.")

if __name__ == "__main__":
  prog_desc = "Read DiSC log file and count the number of sent and "\
    "received DiSC packets."

  parser = argparse.ArgumentParser(description = prog_desc, add_help=False)
  parser.add_argument_group('required arguments')

  # Add a required argument
  parser.add_argument('logfile', help='log_file is the DiSC log.',
    metavar=('<log_file>'), type=str)

  # Help argument
  parser.add_argument('-h','--help',action='help',default=argparse.SUPPRESS,
    help=argparse._('show this help message and exit.'))

  # Rename the arguements' title. The default (i.e. positional arguments)
  # is a bit confusing to users.
  parser._positionals.title = "required arguments"

  args = parser.parse_args()
  main(args)

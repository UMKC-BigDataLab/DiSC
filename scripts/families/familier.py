#!/usr/bin/env python

import sys

def main (args):
  famFilename = args[0]
  numNodes = int(args[1])
  gFam = {}

  for node in range(numNodes):
    gFam[node] = 'Node_'+ str(node+1) +'-0_gspfams='

  famFile = open(famFilename,'r')
  i = 0
  for family in famFile:
    gFam[i] = gFam[i]+family.strip()+';'
    i += 1
    if i == numNodes:
      i = 0

  for node in range(numNodes):
    print(gFam[node])


if __name__ == '__main__':
  main(sys.argv[1:])

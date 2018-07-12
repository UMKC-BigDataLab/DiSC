#!/usr/bin/env python

import sys

def main (args):
  famFilename = args[0]
  numNodes = int(args[1])
  nPerNode = int(args[2])

  gFam = {}

  for node in range(numNodes):
    gFam[node] = 'Node_'+ str(node+1) +'-0_gspfams='

  famFile = open(famFilename,'r')
  i = 0
  n = 0
  for family in famFile:
    if i in gFam:
      gFam[i] = gFam[i]+family.strip()+';'
    n += 1
    if n == nPerNode:
      i += 1
      n = 0

  for node in range(numNodes):
    print(gFam[node])


if __name__ == '__main__':
  main(sys.argv[1:])

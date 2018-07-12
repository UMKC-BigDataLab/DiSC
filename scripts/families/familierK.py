#!/usr/bin/env python

import sys
from random import randint
def main (args):
  famFilename = args[0]
  numNodes = int(args[1])
  K = int(args[2])

  gFam = {}

  for node in range(numNodes):
    gFam[node] = 'Node_'+ str(node+1) +'-0_gspfams='

  famFile = open(famFilename,'r')
  for family in famFile:
    for k in range(K):
      randNode = randint(0, numNodes-1)
      gFam[randNode] = gFam[randNode]+family.strip()+';'

  for node in range(numNodes):
    print(gFam[node])


if __name__ == '__main__':
  main(sys.argv[1:])

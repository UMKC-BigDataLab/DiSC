#!/usr/bin/env python

import sys

def main (args):
  famFilename = args[0]
  famFile = open(famFilename,'r')
  vars = set()
  for family in famFile:
    members = family.strip().split(',')
    for member in members:
      vars.add(member)
  for v in vars:
    print(v)

if __name__ == '__main__':
  main(sys.argv[1:])

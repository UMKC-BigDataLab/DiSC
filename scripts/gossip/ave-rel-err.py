#!/usr/bin/python
# -*- coding: utf-8 -*-
#
# Anas Katib April 19, 2017
#
# Usage: ./ave-rel-err.py input.log gsp_type [-h or --help]

import sys,os
import datetime
import json
import argparse
import time


def eprint(obj):
    sys.stderr.write(str(obj))

def main(args):
  logFilename = args.log
  gossipType  = args.gossiptype
  program_clock = time.time()
  ########################################################
  lineNumber = 0
  initCounterVals = [] # [ {f1:[[][]], f2:[[][]]}, {f1:[[][]], f2:[[][]]}, ... ]
  initCounterNodeIds = []
  estimatedSumVals = {} # { timestamp1 : {f1:[[][]], f2:[[][]], ...} , ... }
  timestampNode = {} # {timestamp1 : "node-id", ...}
  nodeIds = set()
  numEstimates = 0
  startTime = None
  ########################################################
  eprint("Reading log file..")
  start_clock = time.time()
  logFile = open(logFilename,'r')
  for line in logFile:
    lineNumber += 1
    # parse the timestamp
    timestamp = datetime.datetime.strptime(line[6:18], '%H:%M:%S.%f')
    if startTime == None:
      startTime = timestamp
    if startTime > timestamp:
      startTime = timestamp
    # store initial counter values
    if 'INIT C VALS:' in line:
      nodeId = line.split('Node (')[1].split(')')[0]
      initCounterNodeIds.append(nodeId)
      countStr = line.split('VALS:')[1] # split string to get counter's string
      countObj = json.loads(countStr) # parse the string as a JSON object
      initCounterVals.append(countObj)

    # store estimated sum
    elif 'EST C =' in line:
      numEstimates += 1
      try:
        nodeId = line.split('Node (')[1].split(')')[0]
        nodeIds.add(nodeId)
        sumEstStr = line.split('EST C =')[1].strip() # split the string to get the estimate
        sumEstObj = json.loads(sumEstStr) # parse est string as JSON object
        while timestamp in estimatedSumVals: # do not overwrite a previous timestamp
          timestamp +=  datetime.timedelta(microseconds=1)
        estimatedSumVals [timestamp] = sumEstObj
        timestampNode[timestamp] = nodeId
      except:
        eprint("ERROR: PARSING LINE "+str(lineNumber))

  # end for line in logFile
  eprint("["+str(lineNumber)+" lines, " + str(round(time.time() - start_clock, 2))+"s]\n")
  start_clock = time.time()
  eprint("Computing true sum..")
  # compute global true sum for every cell in the family counter #
  familiesTrueSum = {} # { trueSum1 : {}, trueSum2 : {}, ...}

  # number of true sums: one (ALL) or multiple (TAEM)
  trueSumGroups = getTrueSumGroups(gossipType, nodeIds)
  for group in trueSumGroups:
    familiesTrueSum[group] = {} # { f1: [[][]], f2:[[][]], ... }

  for ic in range(len(initCounterNodeIds)):
    for family in initCounterVals[ic]: # initCounterVals[ic] is a peerInitCounter
      # get the group of the peer
      group = getGroupOf(gossipType,initCounterNodeIds[ic])
      if family not in familiesTrueSum[group]: # insert a counter for this family
        familiesTrueSum[group][family] = initCounterVals[ic][family]
      else: # merge new counter with the previous one
        familiesTrueSum[group][family] = sumOf(familiesTrueSum[group][family], initCounterVals[ic][family])
  eprint("[" + str(round(time.time() - start_clock, 2))+"s]\n")
  start_clock = time.time()
  eprint("Computing average relative error..")
  # print sum counter estimations over time #
  # get the list to families (assuming all nodes will have the same family)
  sortedFamilies = sorted(familiesTrueSum[familiesTrueSum.keys()[0]])
  # print output header
  header =  '\t'.join('F'+str(i) for i in xrange(len(sortedFamilies)))
  print("Time\tNode\t"+header)

  timestamp_start_str = validTimestampStr(str(startTime))
  start_dt = datetime.datetime.strptime(timestamp_start_str, '%Y-%m-%d %H:%M:%S.%f')
  for timestamp in sorted(estimatedSumVals):
    timestamp_end_str = validTimestampStr(str(timestamp))
    end_dt = datetime.datetime.strptime(timestamp_end_str, '%Y-%m-%d %H:%M:%S.%f')
    diff = (end_dt - start_dt)
    nodeId = timestampNode[timestamp]
    rowString  = str(diff) +  '\tNode ('+ nodeId+ ')'
    for family in sortedFamilies:
      group = getGroupOf(gossipType,nodeId)
      trueCounter = familiesTrueSum[group][family]
      rowString += '\t' + str(aveCounterRelativeError(trueCounter, estimatedSumVals[timestamp][family]))
    print(rowString)
  eprint("[" + str(round(time.time() - start_clock, 2))+"s].\n")
  eprint("Program finished [" + str(round(time.time() - program_clock, 2))+"s]\n")

# end main

def sumOf(A, B):
  num_rows = len(B)
  num_cols = len(B[0])
  if len(A) != num_rows or len(A[0]) != num_cols:
    return "NOT EQUAL LENGTH"
  for row in range(num_rows):
    for col in range(num_cols):
      A[row][col] += B[row][col]
  return A

def sumCounterCells(counter):
  scc = 0
  for row in counter:
    for cellValue in row:
      scc += cellValue
  return scc

def validTimestampStr(timeStr):
  if '.' not in timeStr:
    timeStr = timeStr+'.0'
  return timeStr

def aveCounterRelativeError (A, B):
  num_rows = len(B)
  num_cols = len(B[0])
  if len(A) != num_rows or len(A[0]) != num_cols:
    return "NOT EQUAL LENGTH"
  counterRelErr = 0
  numRelErr = 0
  for row in range(num_rows):
    for col in range(num_cols):
      numRelErr += 1
      exactValue = A[row][col]
      if exactValue == 0:
        exactValue = 1
      counterRelErr += (abs(exactValue - B[row][col]) / abs(exactValue))*100
  return counterRelErr/numRelErr


def getGroupOf(gspType, id):
  if gspType == 'glbl': # one true sum ALL
    return '0'
  elif gspType == 'npart': # multiple true sums one per node: TEAM (np)
    return id.split('-')[0].strip()
  elif gspType == 'ppart': # multiple true sums one per ring: TEAM (pp)
    return id.split('-')[1].strip()

def getTrueSumGroups(gspType, ids):
  groups = set()
  if gspType == 'glbl': # one true sum ALL
    groups.add(getGroupOf(gspType,list(ids)[0]))
  else: # multiple true sums TEAM
    for id in ids:
      groups.add(getGroupOf(gspType,id))
  return groups



if __name__ == "__main__":
  prog_desc = "Process DiSC log to produce average relative error for all nodes over the"\
  " gossiping timeline.\nThe output is in tab-separated values format (i.e. TSV)"
  parser = argparse.ArgumentParser(description = prog_desc, add_help=False)
  parser.add_argument_group('required arguments')
  parser.add_argument('log', help='input log file (e.g. log.txt).',metavar=('logfile'),
    type=str)
  gossip_types = ["glbl", "npart", "ppart"]
  parser.add_argument('gossiptype', help='type of gossiping in the log file.',
    choices=gossip_types,  type=str)
  parser.add_argument('-h','--help',action='help',default=argparse.SUPPRESS,
    help=argparse._('show this help message and exit.'))
  parser._positionals.title = "required arguments"
  args = parser.parse_args()
  main(args)

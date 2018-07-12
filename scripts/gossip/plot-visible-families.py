#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Anas Katib
# June 9, 2017
#
# Usage: ./plot-visible-families.py -h
# Example: ./plot-visible-families.py log.txt  ./ 00:00:00.000,00:03:00.000
# Note: Also calculates amount of data sent within the interval
import argparse
import sys
import time
from datetime import datetime
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.ticker as plticker


def eprint(obj):
  sys.stderr.write(obj)
  sys.stderr.write('\n')

def main(args):
  lfilename = args.logfile
  ppath = args.plotpath
  interval = args.interval.split(',')
  vfSize = {}
  vfTimestamp = {}
  sTime = time.time()
  lfile = open(lfilename, 'r')
  start = ""
  totalSize = 0
  totalCount = 0
  if '.' not in interval[0]:
    interval[0] += '.0'
  
  if '.' not in interval[1]:
    interval[1] += '.0'

  intervalStartStr = interval[0] if len(interval) == 2 else "00:00:00.000"
  intervalEndStr   = interval[1] if len(interval) == 2 else "23:59:59.999"
  intervalStart = datetime.strptime(intervalStartStr, '%H:%M:%S.%f')
  intervalEnd   = datetime.strptime(intervalEndStr, '%H:%M:%S.%f')
  intervalSize = 0
  intervalCount = 0
  totalAmountOfData = 0
  totalCountOfData = 0
  intervalAmountOfData = 0
  intervalCountOfData = 0
  for line in lfile:
    timestamp = line.strip().split(" ")[1]
    if '.' not in timestamp:
      timestamp +='.0'
    timestamp = datetime.strptime(timestamp, '%H:%M:%S.%f')

    if start == "":
        start = timestamp

    if 'PacketSizeAfter' in line:
      ts = 0
      diff = str(timestamp - start)

      if '.' in diff:
        ts = datetime.strptime(diff, '%H:%M:%S.%f')
      else:
        ts = datetime.strptime(diff, '%H:%M:%S')
      amount = int(line.split('=')[1])

      totalAmountOfData += amount
      totalCountOfData += 1
 
      if ts >= intervalStart and ts <= intervalEnd:
        intervalAmountOfData += amount
        intervalCountOfData += 1

    if "] visible families" in line:
      node_id = line.split("Node (")[1].split(')')[0]
      size = int(line.split('[')[1].split(']')[0])
      totalSize += size
      totalCount += 1
      diff = str(timestamp - start)
      ts = 0
      if '.' in diff:
      	ts = datetime.strptime(diff, '%H:%M:%S.%f')
      else:
      	ts = datetime.strptime(diff, '%H:%M:%S')
      if ts >= intervalStart and ts <= intervalEnd:
          intervalSize += size
          intervalCount += 1


      if node_id not in vfSize:
        vfSize[node_id] = [size]
        vfTimestamp[node_id] = [ts]
      else:
        vfSize[node_id].append(size)
        vfTimestamp[node_id].append(ts)

  aveSizeTitle = "Average Family List Size:"
  overallAve   = "overall: "+str(totalSize/totalCount)
  intervalAve  = "from "+str(intervalStartStr)\
                  +" to "+str(intervalEndStr)+": "
  iAveVal = 0
  if intervalCount > 0:
      iAveVal = str(intervalSize/intervalCount)
  else:
      iAveVal = "NA"

  print(aveSizeTitle)
  print("    "+overallAve)
  print("    "+intervalAve+iAveVal)

  dataTitle = "Amount of Data Sent:"
  overallData   = "overall: "+str(totalAmountOfData)
  intervalData  = "from "+str(intervalStartStr)\
                  +" to "+str(intervalEndStr)+": "+str(intervalAmountOfData)

  print(dataTitle)
  print("    "+overallData)
  print("    "+intervalData)







  fig = plt.figure(figsize=(16,8))
  ax = fig.add_subplot(111)
  eprint('plotting..')
  for node_id in vfSize:
    y = vfSize[node_id]
    # x = [datetime.strptime(elem, '%H:%M:%S.%f')  for elem in vfTimestamp[node_id]]
    x = vfTimestamp[node_id]
    ax.plot(x, y)
  ax.set_xticks(ax.get_xticks()[::1])
  fig.suptitle('Size of Visible Families List on Each Node Over Time ',fontsize=20)
  plt.xlabel('time',fontsize=20)
  plt.ylabel('size',fontsize=20)
  plt.text(0.05, 1.05, aveSizeTitle+"  "+overallAve+",  "+intervalAve+iAveVal,\
          transform=ax.transAxes, fontsize=14,verticalalignment='top')
  fig.savefig(ppath+'/visible-families-size.png')

  eTime = time.time()
  eprint("done in "+str(eTime - sTime) + 's')


if __name__ == "__main__":
  prog_desc = "Plot the size of the family list for all nodes."

  parser = argparse.ArgumentParser(description = prog_desc, add_help=False)
  parser.add_argument_group('required arguments')

  # Add a required argument
  parser.add_argument('logfile', help='contains DiSC log.',
    metavar=('log_file'), type=str)

  parser.add_argument('plotpath', help='location to store the plot.',
    metavar=('plot_path'), type=str)

  # Add optional argument
  parser.add_argument('interval', nargs='?', default="",
    help='a time interval to compute the average size of the '\
     +'visible famlilies lists (optional).', metavar=('start,end'), type=str)

  # Help argument
  parser.add_argument('-h','--help',action='help',default=argparse.SUPPRESS,
    help=argparse._('show this help message and exit.'))

  # Rename the arguements' title. The default (i.e. positional arguments)
  # is a bit confusing to users.
  parser._positionals.title = "execution arguments"

  args = parser.parse_args()
  main(args)

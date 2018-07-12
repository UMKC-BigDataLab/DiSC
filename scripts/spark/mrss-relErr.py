#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Usage: ./mmrs-relErr.py sampleFileCounts fullFileCounts const
# Note: to make this script executable: sudo chmod +x simple.py
# Indent: two spaces.

import sys
import time
import ast
import math

def diff (left, right, factor):
   a = []
   for i in range(len(left)):
      if (right[i] != 0):
         a.append (  math.fabs(left[i]*factor - right[i])/right[i] )
      else:
         a.append (  math.fabs(left[i]*factor - right[i]) )

   return a

def main(args):
  
  if len(args) != 3:
    print("Usage: ./mmrs-relErr.py sampleFileCounts fullFileCounts const")
    return 1

  fp1 = open (args[0],"r")
  fp2 = open (args[1],"r")
  factor = float(args[2])

  left_line  = fp1.readline().rstrip()
  right_line = fp2.readline().rstrip()
  err_sum = 0 
  err_len = 0 
  while left_line  and right_line:
    left_count  = left_line.split(" ")
    right_count = right_line.split(" ")
    if (left_count[0] == right_count[0]):
       left_1  = ast.literal_eval(left_count[1])    
       right_1 = ast.literal_eval(right_count[1])
       r1 = diff(left_1, right_1, factor)

       left_2  = ast.literal_eval(left_count[2])    
       right_2 = ast.literal_eval(right_count[2])
       r2 = diff(left_2, right_2, factor)
      
       fam_sum  =  sum(r1)+sum(r2)
       fam_len  =  len(r1)+len(r2)
       err_sum +=  fam_sum/fam_len
       err_len +=  1 
       print ("Sample      " + left_line)
       print ("True        " + right_line)
       print ("RelErrs     " + left_count[0] +" "+ str(r1) +" " +str(r2))
       print ("AveRelError " + left_count[0] +" "+ str(fam_sum / fam_len))

    else:
       print ("Error: "+left_count[0]+" != "+ right_count[0])

    left_line  = fp1.readline().rstrip()                                            
    right_line = fp2.readline().rstrip()

  fp1.close()
  fp2.close()

  print ("Final relative error average: "+str(err_sum/err_len))

if __name__ == "__main__":
   main(sys.argv[1:])

#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Usage: ./generate-families-k.py <vars_file> <k>

import sys
import time

                                                                                
def powerset(A,varibles,i,k):                                                              
                                                                                
    if i == 0:                                                                  
       return [[A[0]]]                                                          
                                                                                
    ele = A[i]                                                                  
    
    subset = powerset(A,varibles,i-1,k)  
     
    clone  = [x[:] for x in subset]                                                                                                            
    for s in clone:
       n = len(s)                                                       
       if n < k:                                           
              s.append(ele)
       elif n > k:
              clone.remove(s)                                                        
       else:
              mems = []
              for m in s:
                  mems.append(varibles[m])
              
              print(",".join(mems))
              clone.remove(s)                                                        
       
            
                                                                                
    return subset + clone + [[A[i]]]    

def main(args):

    infile = args[0]
    k = int(args[1])

    with open(infile) as f:
      varibles = f.readlines()

    varibles = [x.strip() for x in varibles]
    n = len(varibles)
    A=list(xrange(n))
    pset = powerset(A,varibles,len(A)-1,k)

    
    for fam in pset :
       if len(fam) > 1:
            family = []
            for v in fam:
                family.append(varibles[v])
            print(",".join(family))
    

if __name__ == "__main__":
   main(sys.argv[1:])

#!/usr/bin/python
# -*- coding: utf-8 -*-
#
# Anas Katib July 26, 2016
#
# Usage ./generateHTtable.py <hashtags_file>
# 


import sys, getopt
import os, re
from sets import Set
import io
import json
reload(sys)
sys.setdefaultencoding('utf-8')

def print_err(*args):
    sys.stderr.write(' '.join(map(str,args)) + '\n')

def main(args):
    print ("Reading hashtags from: "+args[0])
    f =  io.open(args[0],'r',encoding='utf-8')
    hashtags = f.read().splitlines()
    f.close()
    # print table header
    print 'tweetId\t',
    for h in hashtags:
        print h,'\t',
    print('')
    print ("Reading tweets from: "+args[1])
    dirPath = os.path.join(args[1])
 
    # read all files in the dir
    for filename in os.listdir(args[1]):
        twtf = io.open(dirPath+'/'+filename,'r',encoding='utf-8')
        for line in twtf:
            tweet = json.loads(line)
            tid = tweet['id']
            tweet_hashtags = tweet['hashtagEntities']
            htset = set()
            for ht in tweet_hashtags:
                htset.add(ht['text'])
        
        twtf.close()
        if(len(htset) > 0):
            print tid,'\t',
            for col in hashtags:
                if col in htset:
                    print '1\t',
                else:
                    print '0\t',
            print ('')
        else:
            htset = None
    
if __name__ == "__main__":
   main(sys.argv[1:])

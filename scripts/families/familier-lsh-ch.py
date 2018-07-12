#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys

try:
    from datasketch import MinHash
except ImportError:
    print ("datasketch is not installed!")
    print ("Run: "+"[sudo] pip install datasketch -U")
    quit()

try:
    from uhashring import HashRing
except ImportError:
    print ("uhashring is not installed!")
    print ("Run: "+"[sudo] pip install uhashring")
    quit()


import hashlib
import numpy as np

def lsh(family, l, k):
    # generate l x k hash values
    n = l * k
    hash_values = MinHash(n)
    for member in family:
        hash_values.update(member.encode('utf8'))

    # create k groups of size l
    groups = [[] for x in xrange(k)]
    s = 0
    e = l
    for g in range(k):
        groups[g] = hash_values.hashvalues[s:e]
        s = e
        e = e + l
    # concatenate each group
    concats = []
    for group in groups:
        g = 0
        maxwidth = 0
        for h in group:
            bi = int(h)
            g = (g << bi.bit_length()) | bi
            # print(bin(h), np.binary_repr(bi))
            # Track max bitlen #######################################
            # ibitlen = g.bit_length()                               #
            # maxwidth =  bitlen if bitlen > maxwidth else maxwidth  #
            ##########################################################
        concats.append(g)

    # create k hashes from the k concatenations groups
    sha1 = hashlib.sha1()
    hashes = []
    for g in range(k):
        hashes.append( hashlib.sha1(str(concats[g])).hexdigest())
        # f = "sha1("+np.binary_repr(concats[g])+")"
        # fx = hashes[g]
        # print( '{0} = {1}'.format(f, fx))

    return hashes


def main(args):

    filename = args[0]
    l   = int(args[1])
    k   = int(args[2])
    n   = int(args[3])

    # create a consistent hash ring of n nodes
    node_names=[str(node) for node in xrange(n)]
    hr = HashRing(nodes=node_names)

    family_map = { str(node):[] for node in xrange(n) }

    for f in open(filename,"r"):
        family = f.replace(" ", "").replace("\n", "")
        fam = family.split(",")

        # get k hashes for this family
        hash_codes  = lsh(fam, l, k)

        # assign k hashes to nodes
        for h in hash_codes:
            node_id = hr.get_node(h)
            family_map[node_id].append(family)

    for node_id in family_map:
        print("Node_"+ str(int(node_id)+1) + "-0_gspfams=" + ";".join(family_map[node_id])+";")
        #sys.stderr.write("Node {node}: {count}\n".format(node=str(int(node_id)+1), count=str(len(family_map[node_id]))))


if __name__ == "__main__":
    if len(sys.argv) != 5:
        print ("  Usage: "+sys.argv[0]+" <family_file> <l> <k> <num_nodes>")
        print ("Example: "+sys.argv[0]+' families.txt  50  2   16')
        quit()

    main(sys.argv[1:])


#!/usr/bin/python
# -*- coding: utf-8 -*-
# encoding=utf8
#
# Anas Katib Novmber 29, 2016
#
usage = """
Usage: ./process-log.py <run_num> <input.log> <gsp_type> [confgfile.txt]
         run_num       An ID number for the experiment. Used for output.
         input.log     Log file of the gossip experiment.
         gsp_type      Type of gossip communications:
			    glbl      (global)
			    npart     (node-partitioned)
			    ppart     (peer-partitioned)
                glbl-part (global-family-partitioned)
"""

import datetime
import random
import sys,os
import json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.dates as dates
import matplotlib.pyplot as plt
import matplotlib.cm as cmx
import matplotlib.colors as colors
import matplotlib.ticker as ticker
from matplotlib.dates import date2num
from matplotlib.offsetbox import AnchoredText
from mpl_toolkits.mplot3d import Axes3D
from shutil import copyfile
import time
import math
font = {'size'   : 22}

matplotlib.rc('font', **font)
reload(sys)
sys.setdefaultencoding('utf8')
#By Ali http://stackoverflow.com/questions/14720331/how-to-generate-random-colors-in-matplotlib
def get_cmap(N):
    '''Returns a function that maps each index in 0, 1, ... N-1 to a distinct
    RGB color.'''
    color_norm  = colors.Normalize(vmin=0, vmax=N-1)
    scalar_map = cmx.ScalarMappable(norm=color_norm, cmap='hsv')
    def map_index_to_rgb_color(index):
        return scalar_map.to_rgba(index)
    return map_index_to_rgb_color

def magnitude(x):
    return int(math.floor(math.log10(x)))

def print_json(data,i):
    if type(data) == dict:
        for k,v in data.items():
            eprint(k+":",i+1)
            print_json(v,i+1)
    else:
        eprint(data,i)

def eprint(obj,indent=None):
    if indent is None:
        indent = 0
    if type(obj) is dict:
        print_json(obj,indent)
        #eprint(json.dumps(obj, sort_keys=True, indent=2))
    else:
        indent_str = ' '*indent
        sys.stderr.write(indent_str+str(obj)+"\n")


def meanMinMax(nums):
    minN = float("inf")
    maxN = float("-inf")
    sumN = 0.0
    numN = 0.0
    for number in nums:
        if number < minN:
            minN = number
        if number > maxN:
            maxN = number
        sumN += number
        numN += 1
    return (sumN/numN), minN, maxN

def stdDeviation(nums,armean):
    variance = 0
    n = 0
    for number in nums:
        variance = variance + (armean - number) ** 2
        n += 1

    return (variance/n) ** 0.5

#def format_time(t):
#	return (float(t.hour) + (float(t.minute)/60) + (float(t.second)/3600) + (float(t.microsecond)/1000000 ))
def getAve(elements, num_elements=-1):
	if num_elements == 0 or len(elements) == 0:
		return 0
	if num_elements == -1:
 		return round(sum(elements) / float(len(elements)),3)
	else:
 		return round(sum(elements) / float(num_elements),3)



def main(args):
        if len(args) < 3:
            eprint(usage)
            return 0
	run_num = args[0]
	inLogName = args[1]
	gossip_type = args[2]
        config_file = ''
	if gossip_type not in ['glbl','npart','ppart','glbl-part']:
	    eprint("\nError: unrecognized gossip type: "+gossip_type)
	    eprint("Valid types are 'glbl', 'npart', 'ppart', or 'glbl-part'.")
	    return 0
	directory = "../run/"+run_num
	if not os.path.exists(directory):
	    os.makedirs(directory)

        #copyfile(args[1], directory+"/"+args[1].split("/")[-1])
        if len(args) > 3 or gossip_type == 'glbl-part':
            try:
                config_file = args[3]
                copyfile(config_file, directory+"/config.txt")
            except:
                eprint("Error: Expecting configuration file as 4th parameter")
                return 1

	inf = open(inLogName,'r')
	ivals = { }
	stime = None
	stime_est = None
	etime = None
	data = { }
	disc_msg_size  = []
	disc_msg_time  = []
	other_msg_size = []
	other_msg_time = []
	sizeBefore = []
	sizeAfter = []
	compTime = []
	decompTime = []
	reductionPercent = []
	column_family  = set()
	msg_count = 0
	algorithm = ''
	line_number = 0
	read_count_times = []
	R = -1
        approxFlag = False
        start_time = time.time()
        read_time  = time.time()
	eprint("Read input log..")
	for line in inf:
		line_number += 1
		try:
			etime = datetime.datetime.strptime(line[6:18], '%H:%M:%S.%f')
			if stime == None:
				stime = etime
			if etime < stime:
				stime = etime
		except:
			print(line+" : "+line[6:18])

		if  "Sending [" in line:# to compute exchanged messages size
			msg_count += 1
			size =  line[line.find("[")+1:line.rfind("]")]
			#log_line = line.split(')',1)
			#size = int(log_line[0].split('(')[1].replace(" bytes","").strip())
			#msg = log_line[1].split(':',1)[1].strip()
			#msg_type = json.loads(msg)["type"]
			#if "UdpGossipDiscMessage" in msg_type:
			disc_msg_size.append(int(size))
			disc_msg_time.append(msg_count)
			#else:
			#	other_msg_size.append(size)
			#	other_msg_time.append(msg_count)

		elif '%SizeReduction' in line:
			reductionPercent.append( float (line.split("=")[1]))
		elif 'MsCompressTime' in line:
			compTime.append( int (line.split("=")[1]))
		elif 'PacketSizeBefore' in line:
			sizeBefore.append( int (line.split("=")[1]))
		elif 'PacketSizeAfter' in line:
			sizeAfter.append( int (line.split("=")[1]))
		elif 'MsDecompressTime' in line:
			decompTime.append( int (line.split("=")[1]))

        	elif  'DiSC:' in line:
			if 'Algorithm Type:' in line:
				algorithm = line.split("Algorithm Type:")[1].rstrip()

			elif 'INIT C VALS:' in line: # store inital values
				node_id = line.split('Node (')[1].split(')')[0]
				CfCountStr = line.split('VALS:')[1]
				Cf_Count_map = json.loads(CfCountStr)
				for cf in Cf_Count_map: # v -> colFam
					if cf not in ivals:
                                            # insert cf into ivals and add a node's counter
                                            ivals[cf] = {node_id:Cf_Count_map[cf]}
					    column_family.add(cf)
					else:
					    # insert the node counter
					    ivals[cf][node_id]=Cf_Count_map[cf]
				data[node_id] = {'times':[], 'values':{},'delays_times':[],'delays':[]}
			elif 'EST C =' in line: # store counts sums
				if stime_est == None:
					stime_est = etime
				node_id = line.split('Node (')[1].split(')')[0]
				sumEst = line.split('EST C =')[1].strip()
				try:
					sumEst = json.loads(sumEst)
					data[node_id]['times'].append(etime)
					for c in sumEst:
						if c not in data[node_id]['values']:
							data[node_id]['values'][c]=[sumEst[c]]
						else:
							data[node_id]['values'][c].append(sumEst[c])
				except:
					eprint("Could not load JSON:")
					eprint(line_number)

			elif 'DELAY:' in line: # store delays
				node_id = line.split('Node (')[1].split(')')[0]
				delay = line.split('DELAY:')[1].strip()
				delay = "{0:.3f}".format(float(delay))
				data[node_id]['delays_times'].append(etime)
				data[node_id]['delays'].append(float(delay))

		elif 'finished reading' in line:
			rc_time = int(line.split()[-1][0:-1])
			read_count_times.append(rc_time)
		elif 'R is set to' in line:
			R_val = line.split()[-1]
			if R == -1:
				R = R_val
			elif R != R_val:
				eprint("Warning: R value is not consistent")
	inf.close()
        eprint("Read input in "+str(int(time.time()-read_time))+"s")
        node_ranks = set()
        peer_ranks = set()
        for pid in data.keys():
            pn = pid.split('-')
            node_ranks.add(pn[0])
            peer_ranks.add(pn[1])

        num_nds = len(node_ranks)
        num_ppn = len(peer_ranks) # number_of_peers_per_node
        eprint('Number of nodes: '+str(num_nds))
        eprint('Number of peers per node: '+str(num_ppn))


        if gossip_type == 'glbl' or gossip_type == 'glbl-part':
          num_groups = 1
        elif gossip_type == 'npart':
          num_groups = num_nds
        elif gossip_type == 'ppart':
          num_groups = num_ppn

        eprint('Number of gossip groups: '+str(num_groups))
        # create the grouping
        grouping = {}
        groups = set('0') if gossip_type == 'glbl' or gossip_type == 'glbl-part' else set()
        for pid in data.keys():
            if gossip_type == 'glbl' or 'glbl-part':
                grouping[pid] = '0' # all peers are in one group
            elif gossip_type == 'npart':
                g = pid.split('-')[0]
                grouping[pid] = g
                groups.add(g)
            elif gossip_type == 'ppart':
                g = pid.split('-')[1]
                grouping[pid] = g
                groups.add(g)
        eprint('Number of Groups: '+str(len(groups)))
        #eprint('Grouping:')
        #eprint(grouping)

	eprint("Calculate true sums..")
	mx_rows = mx_cols = 0

        true_sum_time = time.time()
        # Calculate true sums
	SuperTrues = {}
        #eprint("Init Values:")
        #eprint(ivals)
	#  init true sum counter for every family
	for fmName in ivals:
		len_rows = len(ivals[fmName][ivals[fmName].keys()[0]])
		len_cols = len(ivals[fmName][ivals[fmName].keys()[0]][0])

		if mx_rows < len_rows:
			mx_rows = len_rows

		if mx_cols < len_cols:
			mx_cols = len_cols
                # init a true family counter for every group
                fmTrueGroups = {}
                for g in groups:
                    fmTrueGroups[g] = [[0 for x in range(len_cols)] for y in range(len_rows)]

                # Sum all counters for this family to get the true count
		for peer_id in ivals[fmName]:
			r = c = 0
			for r in range(len_rows):
				for c in range(len_cols):
					fmTrueGroups[grouping[peer_id]] [r][c] +=  ivals[fmName][peer_id][r][c]

			if algorithm=='Ave' : # not updated
				for r in range(len_rows):
					for c in range(len_cols):
						fmTrue[r][c] = fmTrue [r][c] / len(data.keys())
		SuperTrues[fmName] = fmTrueGroups
        #eprint('Super Trues:')
        #eprint(SuperTrues)
        eprint('True sum calculated in '+str(int(time.time() - true_sum_time))+'s')
	output = open(directory +'/output.txt','w')
	counts = open(directory +'/counts.txt','w')
	output.write("Log file: "+inLogName.split("/")[-1])
	output.write("\nAlgorithm: "+algorithm)
	output.write("\nR: "+str(R))
        gTypeStr = 'Global'
        if gossip_type == 'npart':
            gTypeStr = 'Node Partitioned'
        elif gossip_type == 'ppart':
            gTypeStr = 'Peer Partitioned'
        elif gossip_type == 'glbl-part':
            gTypeStr = 'Global Partitioned'
	output.write("\nGossip Type: "+gTypeStr)
	num_peers = len(read_count_times)
	output.write("\nNumber of peers: "+ str(num_peers))
	output.write("\nAve Read & Count Time: "+ str(getAve(read_count_times,num_peers))+" s")
	output.write("\nAve Compression Time: "+ str(getAve(compTime)) +" ms")
	output.write("\nAve Decompression Time: "+ str(getAve(decompTime))+" ms")
	output.write("\nAve Size Before Compression: "+ str(getAve(sizeBefore))+" bytes")
	output.write("\nAve Size After Compression: "+ str(getAve(sizeAfter))+" bytes")
	output.write("\nAve Size Reduction:  "+ str(getAve(reductionPercent))+" %")

	counts.write("\n\nTRUE:\n")
	counts.write(str(SuperTrues))
	counts.write( "\n\nESTIMATED:\n")
	for node_id in data:
		for cf in data[node_id]['values']:
			counts.write("Node: "+node_id+" "+cf.encode('ascii', 'ignore').decode('ascii')+" "+str(data[node_id]['values'][cf][-1])+"\n")
	eprint("Calculate relative error..")
        rel_err_time = time.time()
	counts.write( "\nRELATIVE ERROR:\n")
	for node_id in data:
		for cf in data[node_id]['values']:
 			last_sums = data[node_id]['values'][cf][-1]
			len_rows = len(last_sums)
			len_cols = len(last_sums[0])
			sums = [[0 for x in range(len_cols)] for y in range(len_rows)]
			for r in range(len_rows):
				for c in range(len_cols):
					tsum = SuperTrues[cf][grouping[node_id]][r][c]
					esum = last_sums[r][c]
                                        if tsum != 0:
					    rerr = 100 * abs(float(esum) - tsum)/tsum
                                        elif tsum == 0:
                                            approxFlag = True
					    rerr = 100 * abs(float(esum) - tsum)/(tsum+1)
					sums[r][c] = rerr
			counts.write("Node: "+node_id+" "+cf.encode('ascii', 'ignore').decode('ascii')+" "+str(sums)+"\n")
	output.close()
	counts.close()
        eprint("Relative error calculated in "+str(int(time.time()-rel_err_time))+"s")
	eprint("Acquire relative error details..")
        rel_err_det_time = time.time()
	#output = open(directory +'/relative_error_details.txt','w')
	#output.write("Start Time: "+str(stime.hour)+":"+str(stime.minute)+":"+str(stime.second)+":"+str(stime.microsecond)+"\n\n")
	first_node = last_node = ""
	first_time = datetime.timedelta.max
	last_time  = datetime.timedelta.min
	node_stats = {}
	numFamilies = {}
	node_families = {}
	if gossip_type == 'glbl-part':
	  # get permanent families from file "Node_*-*_gspfams"
	  confFile = open(config_file,'r')
	  for line in confFile:
	    if '_gspfams' in line:
	      node_id = line.split('_gspfams')[0].split('Node_')[1].strip()
	      node_families[node_id] = set(line.split("=")[1].split(";"))
	  confFile.close()
	for node_id in data:
 		node_stats[node_id] = {}
 		numFamilies[node_id] = 0
 		#output.write( "Relative Error Node ("+node_id+"):\n")
 		sum_values = data[node_id]['values']
		for cf in sum_values:
			numFamilies[node_id] += 1
			len_rows = len(sum_values[cf][0])
			len_cols = len(sum_values[cf][0][0])
			node_conv_re = [[float("inf") for x in range(len_cols)] for y in range(len_rows)]
			node_conv_t  = None
			for v in range(len(sum_values[cf])): #for every sum estimate
				c_time = data[node_id]['times'][v] - stime
				if c_time not in node_stats[node_id]:
				    node_stats[node_id][c_time] = []

				sums = [[0 for x in range(len_cols)] for y in range(len_rows)]


				for r in range(len_rows):
					for c in range(len_cols):
                                                tsum = SuperTrues[cf][grouping[node_id]][r][c]
						esum = sum_values[cf][v][r][c]
                                                if tsum != 0:
						    rerr = 100 * abs(float(esum) - tsum)/tsum
                                                elif tsum == 0:
						    rerr = 100 * abs(float(esum) - tsum)/(tsum+1)
                                                    approxFlag = True
						sums[r][c] = rerr
						if gossip_type == "glbl-part":
						  if (cf in node_families[node_id]):
						    node_stats[node_id][c_time].append(rerr)
						else:
                                                    node_stats[node_id][c_time].append(rerr)


				#output.write("\t"+cf.encode('ascii', 'ignore').decode('ascii')+"\t"+str(c_time)+"\t"+str(sums)+"\n")
				#output.write("\t"+str(c_time.hour)+":"+str(c_time.minute)+":"+str(c_time.second)+":"+str(c_time.microsecond)+"\t"+str(sums)+"\n")
				changed = False
				change_type = ""
				update = False
				for r in range(len_rows):
					for c in range(len_cols):
						if  sums[r][c] < node_conv_re[r][c]:
							changed = True
							change_type = "shrunk"
							if ( node_conv_re[r][c] > 10):
								update = True
								node_conv_re = sums
								node_conv_t  = c_time
								break

						elif  sums[r][c] > node_conv_re[r][c]:
							changed = True
							change_type = "grew"# still haven't converged
							update = True
							node_conv_re = sums
							node_conv_t  = c_time
							break
					if (update):
						break
			if (node_conv_t < first_time):
				first_time = node_conv_t
				first_node = node_id
			if (node_conv_t > last_time):
				last_time = node_conv_t
				last_node = node_id
 		#print(node_stats[node_id])
	#output.close()
        eprint('Relative error details acquired in '+str(int(time.time()-rel_err_det_time))+"s")
	eprint("Calculate statistics..")
        stat_time = time.time()
	output = open(directory +'/relative_error_statistics.txt','w')
	output.write("Time          \tMean  \tStdDev\tMin   \tMax\n")
	nodesMeanErrs = {}
	maxMeanErr = 0
	for node_id in sorted(node_stats,key=lambda k: (int(k.split("-")[0]),int(k.split("-")[1]) )):
	    nodesMeanErrs[node_id] = {"times":[], "merate":[]}
	    output.write("Node ("+node_id+"): \n")
	    ts_counter = 0
	    ts = mean_error = min_error = max_error = std_error = 0
	    num_tstamps = len(node_stats[node_id])
            ts_interval = 10
            if num_tstamps <= 7:
                ts_interval = 1
            elif num_tstamps <= 20:
                ts_interval = 3
            elif num_tstamps <= 40:
                ts_interval = 7
    
	    for ts in sorted(node_stats[node_id]):
	        base_err_stats = meanMinMax(node_stats[node_id][ts])
	        stdv_err = stdDeviation(node_stats[node_id][ts],base_err_stats[0])

	        mean_error = str(format(round(base_err_stats[0],3),'.3f'))
	        min_error = str(format(round(base_err_stats[1],3),'.3f'))
	        max_error = str(format(round(base_err_stats[2],3),'.3f'))
		if maxMeanErr < base_err_stats[2]:
			maxMeanErr = base_err_stats[2]
	        std_error = str(format(round(stdv_err,3),'.3f'))
		timeStampString = str(ts)
		if '.' not in timeStampString:
			timeStampString = timeStampString+'.0'
	        time_stamp = date2num(datetime.datetime.strptime(timeStampString, '%H:%M:%S.%f'))

	        nodesMeanErrs[node_id]["times"].append(time_stamp)
	        nodesMeanErrs[node_id]["merate"].append(float(mean_error))
		if ts_counter % ts_interval == 0 or ts_counter == num_tstamps:
	        	output.write(str(ts) +"\t" + mean_error  +"\t" +std_error+"\t"+min_error+"\t"+max_error +"\n")
	        ts_counter += 1
	output.close()

	output = open(directory +'/output.txt','a')
	output.write("\n\nFirst Convergence (without an increased relative error in subsequent estimates): ")
	output.write("\nFirst Node  Node ("+first_node+") at "+str(first_time))
	output.write("\nLast  Node  Node ("+last_node+") at "+str(last_time))
	output.close()

	dur  = str(etime - stime)
	durf = datetime.datetime.strptime(dur, '%H:%M:%S.%f')
	dur  = "{:%Mm %Ss}".format(durf)
        eprint('Statisics calculated in '+str(int(time.time()-stat_time))+"s")

        eprint("Plot mean realtive errors..")
	# draw delays
	fig5 = plt.figure(figsize=(20,10))
	col_labels=[]
	table_vals=[[]]
	plt.figtext(0.02,0.97,'[Duration:'+dur+']')
	plt.figtext(0.43,0.97,'Number of Families')
	for node_id in sorted(nodesMeanErrs,key=lambda k: (int(k.split("-")[0]),int(k.split("-")[1]) )):
		col_labels.append(node_id)
		table_vals[0].append(numFamilies[node_id])
		plt.plot(nodesMeanErrs[node_id]["times"], nodesMeanErrs[node_id]["merate"], label=node_id, lw=6)
	the_table = plt.table(cellText=table_vals,colLabels=col_labels,loc='upper center',bbox=[0, 1.01, 1, 0.1])

	plt.xlabel('Time')
	plt.ylabel('Mean Relative Error')
	#plt.grid()
	ax = fig5.add_subplot(1,1,1)
	tickInterval = 10 ** (magnitude(maxMeanErr))
	loc = ticker.MultipleLocator(base=tickInterval)
	ax.yaxis.set_major_locator(loc)
	ax.legend(loc='center left', bbox_to_anchor=(1, 0.5),  title="Nodes")
	#plt.show()
	plt.savefig(directory +'/mean-relative-errors.png')
	plt.clf()

	eprint("Plot delays..")
	# draw delays
	plt.figure(figsize=(16,8))
	fig3 = plt.figure(figsize=(16,8))
	for node_id in data:
		plt.plot(data[node_id]['delays_times'], data[node_id]['delays'], label=node_id, lw=2)

	plt.xlabel('Time')
	plt.ylabel('Delays')
	plt.grid()
	ax = fig3.add_subplot(1,1,1)
	loc = ticker.MultipleLocator(base=1.0) # this locator puts ticks at regular intervals
	ax.yaxis.set_major_locator(loc)
	plt.legend(loc='upper right')
	#plt.show()
	#plt.gcf().autofmt_xdate()
	plt.savefig(directory +'/delays.png')
	plt.clf()

	eprint("Plot message sizes..")
	# plot message sizes
	fig4 = plt.figure(figsize=(16,8))
	plt.scatter(disc_msg_time,disc_msg_size, label="DiSC", marker='o', color='g')
	#plt.scatter(other_msg_time,other_msg_size, label="Other", marker='s', color='r')

	sumMS = 0
	for element in disc_msg_size:
    		sumMS+=element
	ave_disc_msg_size = 0
	num_disc_msgs = len(disc_msg_size)
	if num_disc_msgs > 0:
		ave_disc_msg_size  = sumMS/num_disc_msgs

	txt = "Total DiSC Msgs Size: "+str(sumMS/1000)+"MB"
	sumOMS = 0
	#for element in other_msg_size:
    	#	sumOMS+=element
	#ave_other_msg_size = 0
	#if len(other_msg_size) > 0:
	#	ave_other_msg_size = sumOMS/len(other_msg_size)

	#txt += "\nAve DiSC Msg. Size: "+str(ave_disc_msg_size)+"B   Ave Other Msg. Size: "+str(ave_other_msg_size)+"B"
	txt += "\nAve DiSC Msg. Size: "+str(ave_disc_msg_size)+"B"
	plt.annotate(txt, xy=(0.3, 0.5), xycoords='axes fraction')

	plt.xlabel('Timeline')
	plt.ylabel('Message Size (Bytes)')
	#plt.grid()
	plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=2, mode="expand", borderaxespad=0.)
	plt.gcf().autofmt_xdate()
	plt.savefig(directory +'/msg-sizes.png')
	plt.clf()
	plt.close('all')

	# define plot and color map
	clrmap = get_cmap(len(data)*mx_rows*mx_cols)
        output = open(directory +'/output.txt','a')
	output.write("\nNumber of DiSC messages: "+str(num_disc_msgs))
	output.write("\nAve size of DiSC messages: "+str(ave_disc_msg_size)+" bytes")
	output.write("\nDuration: "+str(dur)+"\n")
	output.close()
	eprint("INITIAL PROCESSING "+inLogName+" DONE. ["+str(int(time.time()-start_time))+"s]")
	if (True): return 0

	for cf in column_family:
		if not os.path.exists(directory+"/colFam_"+cf.encode('ascii', 'ignore').decode('ascii')):
    			os.makedirs(directory+"/colFam_"+cf.encode('ascii', 'ignore').decode('ascii'))


	eprint("Plot families' relative errors and convergence..")
	fcount = 1
	fall = len(column_family)
	ave_t = 0
	total_t = 0
        #for group_name in groups:
	for cf in column_family:
		start = time.time()
		# Assuming all nodes have the same column families
		any_node_id = data.keys()[0]
		len_rows = len(data[any_node_id]['values'][cf][0])
		len_cols = len(data[any_node_id]['values'][cf][0][0])

		fig1   = plt.figure(figsize=(16,8))
		ax     = fig1.add_subplot(111, projection='3d')
		ax.set_xlabel('Time Instance')
		ax.set_ylabel('Cell')
		ax.set_zlabel(algorithm)

		plt.grid()
		#plt.gcf().autofmt_xdate()

		txt = algorithm +' Convergence\nDuration: '+dur
		plt.annotate(txt, xy=(0.05, 0.90), xycoords='axes fraction')

		# plot estimated sums
		t1 = 0
		for node_id in data:
			# create ROWS x COLS values matrix for the node

			eSUMS  =  [[[] for x in range(len_cols)] for y in range(len_rows)]
			eTIMES =  [[[] for x in range(len_cols)] for y in range(len_rows)]
			eCs    =  [[[] for x in range(len_cols)] for y in range(len_rows)]
			C      =  data[node_id]['values'][cf]
			T      =  [x for x in range(len(data[node_id]['times']))]
			t1     =  max(t1,T[-1])
			for v in range(len(C)):
				val = C[v]
				for r in range(len_rows):
					for c in range(len_cols):
						eSUMS[r][c].append(val[r][c])
						eTIMES[r][c].append(T[v])
						eCs[r][c].append((r * len_cols) + c)

			for r in range(len_rows):
				for c in range(len_cols):
					z  =  (r * len_cols) + c
					T  = eTIMES[r][c]
					S  = eSUMS[r][c]
					Ci = eCs[r][c]
					ax.plot(T,Ci,S, label="Node("+node_id+")_"+str(z), lw=2,c=clrmap(z))

		# plot true sums
                for group_name in groups:
		    for r in range(len_rows):
			for c in range(len_cols):
				tsum = SuperTrues[cf][group_name][r][c]
				z  =  (r * len_cols) + c
				T  = [0,t1]
				S  = [tsum,tsum]
				Ci = [z,z]
				ax.plot(T,Ci,S,label="True_"+group_name+"_"+str(z), lw=1,c="b")

		plt.legend(bbox_to_anchor=(0.00,1.05))
		#plt.show()

		plt.savefig(directory+"/colFam_"+ cf.encode('ascii', 'ignore').decode('ascii') +'/convergence.png')
        	plt.close()
		# draw relative error for estimated sums
		fig2 = plt.figure(2,figsize=(16,8))
		#fig2 = plt.figure(2)
		ax   = fig2.add_subplot(111, projection='3d')
		ax.set_xlabel('Time Instance')
		ax.set_ylabel('Cell')
		ax.set_zlabel('Relative Error')

		plt.grid()
		#plt.gcf().autofmt_xdate()

		txt = 'Relative Error'
		plt.annotate(txt, xy=(0.05, 0.90), xycoords='axes fraction')


		# calculate relative error
		t1 = 0
		for node_id in data:
			# create ROWS x COLS values matrix for the node
			rERR   =  [[[] for x in range(len_cols)] for y in range(len_rows)]
			rTIMES =  [[[] for x in range(len_cols)] for y in range(len_rows)]
			rCs    =  [[[] for x in range(len_cols)] for y in range(len_rows)]
			C  = data[node_id]['values'][cf]
			T  = [x for x in range(len(data[node_id]['times']))]
			t1 = max(t1,T[-1])
			for v in range(len(C)):
				val = C[v]
				for r in range(len_rows):
					for c in range(len_cols):
						esum =  val[r][c]
						tsum =  SuperTrues[cf][r][c]
						rerr =100 * abs(float(esum) - tsum)/tsum
						rERR[r][c].append(rerr)
						rTIMES[r][c].append(T[v])
						rCs[r][c].append((r * len_cols) + c)

			for r in range(len_rows):
				for c in range(len_cols):
					z  =  (r * len_cols) + c
					T  = rTIMES[r][c]
					S  = rERR[r][c]
					Ci = rCs[r][c]
					ax.plot(T,Ci,S, label="Node("+node_id+")_"+str(z), lw=2,c=clrmap(z))


		plt.legend(bbox_to_anchor=(0.0,1.05))
		#plt.show()
		plt.savefig(directory+"/colFam_"+ cf.encode('ascii', 'ignore').decode('ascii') +'/relative-error.png')
        	plt.close()
		total_t += (time.time() - start)
		ave_t = total_t / fcount
		est_t = ave_t * ( fall - fcount)
	    	eprint("    Plotted "+str(fcount)+" of "+ str(fall)+ ". Estimated remaining time: "+"{0:.2f}".format(est_t)+"s" )
		fcount += 1

if __name__ == "__main__":
   main(sys.argv[1:])


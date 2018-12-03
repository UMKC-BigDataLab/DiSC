package edu.umkc.Compute;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class ErrorCalculator {
	public static String calculateError(Map<String, LinkedList<LinkedList<Double>>> actualCounts,
			Map<String, LinkedList<LinkedList<Double>>> estimatedCounts, String family, long startTime) {
		String errorStat = null;
		for (String key : actualCounts.keySet()) {
			if(key.equals(family)) {
				int counter = 0;
				Double errPercent = 0d;
				Iterator<LinkedList<Double>> it1 = actualCounts.get(key).iterator();
				Iterator<LinkedList<Double>> it2 = estimatedCounts.get(key).iterator();

				while (it1.hasNext() && it2.hasNext()) {
					Iterator<Double> actCountArr = it1.next().iterator();
					Iterator<Double> estCountArr = it2.next().iterator();

					while (actCountArr.hasNext() && estCountArr.hasNext()) {
						Double estCnt = estCountArr.next();
						Double actCnt = actCountArr.next();

						if (actCnt != 0) {
							counter += 1;
							errPercent += Math.abs(estCnt - actCnt) *100 / actCnt;
						}
					}
				}
				errorStat = ((System.currentTimeMillis() - startTime)/1000) + "," + errPercent/counter;
				break;
			}
		}
		return errorStat; 
	}
}
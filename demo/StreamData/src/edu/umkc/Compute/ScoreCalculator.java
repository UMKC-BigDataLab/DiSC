package edu.umkc.Compute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.math3.special.Gamma;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umkc.Constants.DiSCConstants;
import edu.umkc.Util.CommonUtil;
import edu.umkc.Util.PropertyReader;

public class ScoreCalculator {
	private static final Logger logger = LogManager.getLogger(ScoreCalculator.class.getName());
		
	private static Double getScore(Double[][] inp) {
		//Assuming prior as a matrix with the values as 1.
		Double[][] prior = new Double[inp.length][inp[0].length];
		
		for(int i=0; i<inp.length; i++) {
			for(int j=0; j<inp[0].length; j++) {
				prior[i][j] = 1.0;
			}
		}
		
		Double score = 0d;
		
		for(int i=0; i<inp[0].length; i++) {
			Double sumM = inp[0][i] + inp[1][i];
			Double sumAplha = prior[0][i] + prior[1][i];
			score += Gamma.logGamma(sumAplha) - Gamma.logGamma(sumAplha + sumM);
			//score += Math.log10(sumAplha/(sumAplha + sumM));
		}
		
		for(int i=0; i<inp.length; i++) {
			for(int j=0; j<inp[0].length; j++) {
				score += Gamma.logGamma(prior[i][j] + inp[i][j]) - Gamma.logGamma(prior[i][j]);
				//score += Math.log10((prior[i][j] + inp[i][j])/prior[i][j]);
			}
		}
		
		return score;
	}
	
	public static String getActAndEstScores(String family) {
		Double estScore = null;
		Double actScore = null;
		logger.debug("ScoreCalculator :: getActAndEstScores :: Start");
		try (BufferedReader br = new BufferedReader(new FileReader(new File(DiSCConstants.EST_C_FILE)))) {
			Map<String, LinkedList<LinkedList<Double>>> estimatedCounts = CommonUtil.convertJsonToMap(br.readLine());
			for (String key : estimatedCounts.keySet()) {
				if (key.equals(family)) {
					logger.debug("ScoreCalculator :: getActAndEstScores :: Estimated Count Matrix :: " + estimatedCounts.get(key));
					estScore = getScore(CommonUtil.convertToMatrix(estimatedCounts.get(key)));
					logger.debug("ScoreCalculator :: getActAndEstScores :: Estimaged Score Calculated :: " + estScore);
					break;
				}
			}
		} catch (Exception e) {
			logger.error("ScoreCalculator :: Exception encountered while calculating Estimated Score:: " + e);
			e.printStackTrace();
		}

		try (BufferedReader br = new BufferedReader(new FileReader(new File(PropertyReader.getInstance().getProperty(DiSCConstants.TRUE_COUNTS_FILE))))) {
			Map<String, LinkedList<LinkedList<Double>>> trueCounts = CommonUtil.convertJsonToMap(br.readLine());
			for (String key : trueCounts.keySet()) {
				if (key.equals(family)) {
					logger.debug("ScoreCalculator :: getActAndEstScores :: Actual Count Matrix :: " + trueCounts.get(key));
					actScore = getScore(CommonUtil.convertToMatrix(trueCounts.get(key)));
					logger.debug("ScoreCalculator :: getActAndEstScores :: Actual Score Calculated :: " + actScore);
					break;
				}
			}
		} catch (Exception e) {
			logger.error("ScoreCalculator :: Exception encountered while calculating Actual Score :: " + e);
			e.printStackTrace();
		}
		
		return "{\"EstScore\":" + estScore + ",\"ActScore\":" + actScore + "}";
	}
}

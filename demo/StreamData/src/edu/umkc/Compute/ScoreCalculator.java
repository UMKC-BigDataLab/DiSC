package edu.umkc.Compute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
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

	public static String getActAndEstScores(String family, String scoringFunction, String ess) {
		String estScore = null;
		String actScore = null;
		logger.debug("ScoreCalculator :: getActAndEstScores :: Start");
		try (BufferedReader br = new BufferedReader(new FileReader(new File(DiSCConstants.EST_C_FILE)))) {
			Map<String, LinkedList<LinkedList<Double>>> estimatedCounts = CommonUtil.convertJsonToMap(br.readLine());
			for (String key : estimatedCounts.keySet()) {
				if (key.equals(family)) {
					logger.debug("ScoreCalculator :: getActAndEstScores :: Estimated Count Matrix :: " + estimatedCounts.get(key));
					if (DiSCConstants.BDeu.equals(scoringFunction)) {
						estScore = BDeuScoreCalculator.getInstance().getScore(CommonUtil.convertToMatrix(estimatedCounts.get(key)), Double.parseDouble(ess));
					} else if (DiSCConstants.K2.equals(scoringFunction)) {
						estScore = K2ScoreCalculator.getInstance().getScore(CommonUtil.convertToMatrix(estimatedCounts.get(key)));
					}
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
					if (DiSCConstants.BDeu.equals(scoringFunction)) {
						actScore = BDeuScoreCalculator.getInstance().getScore(CommonUtil.convertToMatrix(trueCounts.get(key)), Double.parseDouble(ess));
					} else if (DiSCConstants.K2.equals(scoringFunction)) {
						actScore = K2ScoreCalculator.getInstance().getScore(CommonUtil.convertToMatrix(trueCounts.get(key)));
					}
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

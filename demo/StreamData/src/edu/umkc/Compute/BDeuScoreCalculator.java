package edu.umkc.Compute;

import org.apache.commons.math3.special.Gamma;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;


public class BDeuScoreCalculator {
    private static final Logger logger = LogManager.getLogger(BDeuScoreCalculator.class.getName());
    private static BDeuScoreCalculator instance = null;

    private BDeuScoreCalculator() {

    }

    public static BDeuScoreCalculator getInstance() {
        if (instance == null) {
            instance = new BDeuScoreCalculator();
        }
        return instance;
    }

    public String getScore(Double[][] inp, Double inpESS) {
        logger.debug("BDeuScoreCalculator :: getScore :: Start");
        Double[][] prior = new Double[inp.length][inp[0].length];
        Double ess = inpESS/(inp.length * inp[0].length);

        for(int i=0; i<inp.length; i++) {
            for(int j=0; j<inp[0].length; j++) {
                prior[i][j] = ess;
            }
        }

        Double score = 0d;

        for(int i=0; i<inp[0].length; i++) {
            Double sumM = inp[0][i] + inp[1][i];
            Double sumAplha = prior[0][i] + prior[1][i];
            score += Gamma.logGamma(sumAplha) - Gamma.logGamma(sumAplha + sumM);
        }

        for(int i=0; i<inp.length; i++) {
            for(int j=0; j<inp[0].length; j++) {
                score += Gamma.logGamma(prior[i][j] + inp[i][j]) - Gamma.logGamma(prior[i][j]);
            }
        }

        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(score);
    }

}

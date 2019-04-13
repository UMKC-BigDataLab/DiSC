package edu.umkc.Compute;

import org.apache.commons.math3.special.Gamma;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;

public class K2ScoreCalculator {
    private static final Logger logger = LogManager.getLogger(K2ScoreCalculator.class.getName());
    private static K2ScoreCalculator instance = null;

    private K2ScoreCalculator() {

    }

    public static K2ScoreCalculator getInstance() {
        if (instance == null) {
            instance = new K2ScoreCalculator();
        }
        return instance;
    }

    public String getScore(Double[][] inp) {
        logger.debug("K2ScoreCalculator :: getScore :: Start");

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

        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(score);
    }

}

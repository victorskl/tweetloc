package com.sankholin.comp90049.project1.editdistance;

import opennlp.tools.ngram.NGramModel;
import opennlp.tools.util.StringList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NGramDistance implements StringDistance {

    private int minNGramLength = 2;
    private int maxNGramLength = 2;

    @Override
    public int getDistance(String s1, String s2) {
        NGramModel aTweetNGram = buildNGrams(s1, minNGramLength, maxNGramLength);
        NGramModel aGazetteerNGram = buildNGrams(s2, minNGramLength, maxNGramLength);
        return computeNGramsDistance(aTweetNGram, aGazetteerNGram);
    }

    private NGramModel buildNGrams(String str, int minN, int maxN) {
        NGramModel aNGramModel = new NGramModel();
        aNGramModel.add(str, minN, maxN);

        /*
        if (logger.getLevel().name().equalsIgnoreCase("TRACE")) {
            StringBuilder sb = new StringBuilder();
            for (StringList strList : aNGramModel) {
                sb.append(strList.toString()).append(" ");
                //System.out.print(strList.toString() + " ");
            }
            //System.out.println();
            logger.trace(sb.toString());
        }
        */
        return aNGramModel;
    }

    private int computeNGramsDistance(NGramModel aTweetNGramModel, NGramModel aGazetteerNGramModel) {
        int intersect = 0;
        //StringBuilder sb = new StringBuilder();
        for (StringList strList : aTweetNGramModel) {
            if (aGazetteerNGramModel.contains(strList)) {
                ++intersect;
                //sb.append("*");
                //System.out.print("*");
            }
            //sb.append(strList.toString()).append(" ");
            //System.out.print(strList.toString().concat(" "));
        }
        //logger.trace(sb.toString());

        int sum = (aGazetteerNGramModel.size() + aTweetNGramModel.size()) - 2 * intersect;
        //logger.trace("ngram distance: " + sum + ", intersect: " + intersect);
        return sum;
    }

    public int getMinNGramLength() {
        return minNGramLength;
    }

    public void setMinNGramLength(int minNGramLength) {
        this.minNGramLength = minNGramLength;
    }

    public int getMaxNGramLength() {
        return maxNGramLength;
    }

    public void setMaxNGramLength(int maxNGramLength) {
        this.maxNGramLength = maxNGramLength;
    }

    private static final Logger logger = LogManager.getLogger(NGramDistance.class);
}

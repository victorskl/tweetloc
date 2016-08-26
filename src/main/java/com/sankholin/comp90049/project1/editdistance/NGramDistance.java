package com.sankholin.comp90049.project1.editdistance;

import com.sankholin.comp90049.project1.StringSearch;
import opennlp.tools.ngram.NGramModel;
import opennlp.tools.util.StringList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NGramDistance implements StringSearch {

    private int minNGramLength = 2;
    private int maxNGramLength = 2;

    @Override
    public int getScore(String s1, String s2) {
        NGramModel aTweetNGram = buildNGrams(s1, minNGramLength, maxNGramLength);
        NGramModel aGazetteerNGram = buildNGrams(s2, minNGramLength, maxNGramLength);
        return computeNGramsDistance(aTweetNGram, aGazetteerNGram);
    }

    private NGramModel buildNGrams(String str, int minN, int maxN) {
        NGramModel aNGramModel = new NGramModel();
        aNGramModel.add(str, minN, maxN);
        return aNGramModel;
    }

    private int computeNGramsDistance(NGramModel aTweetNGramModel, NGramModel aGazetteerNGramModel) {
        int intersect = 0;
        for (StringList strList : aTweetNGramModel) {
            if (aGazetteerNGramModel.contains(strList)) {
                ++intersect;
            }
        }

        return (aGazetteerNGramModel.size() + aTweetNGramModel.size()) - 2 * intersect;
    }

    public int getMinNGramLength() {
        return minNGramLength;
    }

    public void setMinNGramLength(int minNGramLength) {
        if (minNGramLength > 0) this.minNGramLength = minNGramLength;
    }

    public int getMaxNGramLength() {
        return maxNGramLength;
    }

    public void setMaxNGramLength(int maxNGramLength) {
        if (maxNGramLength > 0) this.maxNGramLength = maxNGramLength;
    }

    private static final Logger logger = LogManager.getLogger(NGramDistance.class);
}

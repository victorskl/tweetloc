package com.sankholin.comp90049.project1.editdistance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GenericDistanceImpl {

    private int match;
    private int insertion;
    private int deletion;
    private int replace;

    /**
     * If levenshtein is true, will compute based on Levenshtein Distance (minimizing), otherwise
     * will compute based on Needleman–Wunsch algorithm (maximizing).
     *
     */
    int computeGlobalEditDistance(CharSequence f, CharSequence t, boolean levenshtein) {
        int lf = f.length() + 1, lt = t.length() + 1; //bec of Epsilon coord(0,0)=0, need to increase array size by +1
        logger.trace(lf + "x" +lt);

        int[][] array = new int[lf][lt];
        array[0][0] = 0; // initialise Epsilon; but note that we still are zero base array

        for (int j = 1; j < lf; j++) array[j][0] = j * insertion; //insertion
        for (int k = 1; k < lt; k++) array[0][k] = k * deletion; //deletion

        for (int j = 1; j < lf; j++)
            for (int k = 1; k < lt; k++)
                if (levenshtein) {
                    array[j][k] = minimum(
                            array[j][k - 1] + deletion,
                            array[j - 1][k] + insertion,
                            array[j - 1][k - 1] + ((f.charAt(j - 1) == t.charAt(k - 1)) ? match : replace));
                } else {
                    array[j][k] = maximum(
                            array[j][k - 1] + deletion,
                            array[j - 1][k] + insertion,
                            array[j - 1][k - 1] + ((f.charAt(j - 1) == t.charAt(k - 1)) ? match : replace));
                }

        return array[lf-1][lt-1]; // zero base index, so need max length -1
    }

    private int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    private int maximum(int a, int b, int c) {
        return Math.max(Math.max(a, b), c);
    }

    public void printScore() {
        String msg = "Edit Distance Score Parameter: match = " + match + ", insertion = " + insertion
                + ", deletion = " + deletion + ", replace = " + replace;
        logger.info(msg);
    }

    public int getMatch() {
        return match;
    }

    public void setMatch(int match) {
        this.match = match;
    }

    public int getInsertion() {
        return insertion;
    }

    public void setInsertion(int insertion) {
        this.insertion = insertion;
    }

    public int getDeletion() {
        return deletion;
    }

    public void setDeletion(int deletion) {
        this.deletion = deletion;
    }

    public int getReplace() {
        return replace;
    }

    public void setReplace(int replace) {
        this.replace = replace;
    }

    private static final Logger logger = LogManager.getLogger(GenericDistanceImpl.class);
}

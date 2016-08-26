package com.sankholin.comp90049.project1.editdistance;

import com.sankholin.comp90049.project1.tool.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GenericEditDistanceImpl {

    private int match;
    private int insertion;
    private int deletion;
    private int replace;

    private Utilities util = Utilities.getInstance();

    /**
     * Global Edit Distance, Needleman–Wunsch algorithm - optimal global alignment
     * Levenshtein Distance, if match=0 && insert=1 && delete=1 && replace=1
     *
     */
    int computeGlobalEditDistance(CharSequence f, CharSequence t) {
        int lf = f.length() + 1, lt = t.length() + 1; //bec of Epsilon e(0,0)=0, need to increase array size by +1

        int[][] array = new int[lf][lt];
        array[0][0] = 0; // initialise Epsilon e; but note that we still are zero based array

        for (int j = 1; j < lf; j++) array[j][0] = j * insertion; //insertion
        for (int k = 1; k < lt; k++) array[0][k] = k * deletion; //deletion

        for (int j = 1; j < lf; j++) { // still zero based index, already initialized e(0,0) = 0
            for (int k = 1; k < lt; k++) {
                if (isMinimalScore()) {
                    array[j][k] = util.min(
                            array[j][k - 1] + deletion,
                            array[j - 1][k] + insertion,
                            array[j - 1][k - 1] + ((f.charAt(j - 1) == t.charAt(k - 1)) ? match : replace));
                } else {
                    array[j][k] = util.max(
                            array[j][k - 1] + deletion,
                            array[j - 1][k] + insertion,
                            array[j - 1][k - 1] + ((f.charAt(j - 1) == t.charAt(k - 1)) ? match : replace));
                }
            }
        }
        return array[lf-1][lt-1]; // zero based index, length -1 for e(0,0)
    }

    /**
     * Local Edit Distance, Smith-Waterman algorithm - local sequence alignment
     *
     */
    int computeLocalEditDistance(CharSequence f, CharSequence t) {
        int greatest = Integer.MIN_VALUE, smallest = Integer.MAX_VALUE;
        int lf = f.length() + 1, lt = t.length() + 1;

        int[][] array = new int[lf][lt];

        array[0][0] = 0;
        for (int j = 1; j < lf; j++) array[j][0] = 0;
        for (int k = 1; k < lt; k++) array[0][k] = 0;

        for (int j = 1; j < lf; j++) {
            for (int k = 1; k < lt; k++) {
                if (isMinimalScore()) {
                    int result = util.min(
                            0,
                            array[j][k - 1] + deletion,
                            array[j - 1][k] + insertion,
                            array[j - 1][k - 1] + ((f.charAt(j - 1) == t.charAt(k - 1)) ? match : replace));
                    array[j][k] = result;
                    if (result < smallest)
                        smallest = result;
                } else {
                    int result = util.max(
                            0,
                            array[j][k - 1] + deletion,
                            array[j - 1][k] + insertion,
                            array[j - 1][k - 1] + ((f.charAt(j - 1) == t.charAt(k - 1)) ? match : replace));
                    array[j][k] = result;
                    if (result > greatest)
                        greatest = result;
                }
            }
        }

        return (isMinimalScore() ? smallest : greatest);
    }

    public boolean isMinimalScore() {
        return match < insertion && match < deletion && match < replace;
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

    private static final Logger logger = LogManager.getLogger(GenericEditDistanceImpl.class);
}

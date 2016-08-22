package com.sankholin.comp90049.project1.editdistance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Levenshtein Distance
 */
public class LevenshteinDistance extends GenericDistanceImpl implements StringDistance {

    @Override
    public int getDistance(String s1, String s2) {
        return computeGlobalEditDistance(s1, s2, true);
    }

    private static final Logger logger = LogManager.getLogger(LevenshteinDistance.class);
}
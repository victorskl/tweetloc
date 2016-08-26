package com.sankholin.comp90049.project1.editdistance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocalEditDistance extends GenericEditDistanceImpl implements EditDistance {

    @Override
    public int getScore(String s1, String s2) {
        return computeLocalEditDistance(s1, s2);
    }

    private static final Logger logger = LogManager.getLogger(LocalEditDistance.class);
}

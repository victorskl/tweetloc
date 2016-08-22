package com.sankholin.comp90049.project1.editdistance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Needleman–Wunsch algorithm
 */
public class NWEditDistance extends GenericDistanceImpl implements StringDistance{

    @Override
    public int getDistance(String s1, String s2) {
        return computeGlobalEditDistance(s1, s2, false);
    }

    private static final Logger logger = LogManager.getLogger(NWEditDistance.class);
}

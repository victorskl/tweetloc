package com.sankholin.comp90049.project1.tool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton Utilities
 */
public final class Utilities {

    private static Utilities instance = null;
    private Utilities() {}
    public static synchronized Utilities getInstance() {
        if (instance == null) instance = new Utilities();
        return instance;
    }

    public List<String> tokenizeString(Analyzer analyzer, String string) {
        return tokenizeString(analyzer, null, string);
    }

    private List<String> tokenizeString(Analyzer analyzer, String field, String string) {
        List<String> result = new ArrayList<>();
        try {
            TokenStream stream = analyzer.tokenStream(field, new StringReader(string));
            stream.reset();
            while (stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
            stream.end();
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public int min(int a, int b, int... more) {
        int min = Math.min(a, b);
        for (int m : more)
            min = Math.min(min, m);
        return min;
    }

    public int max(int a, int b, int... more) {
        int max = Math.max(a, b);
        for (int m : more)
            max = Math.max(max, m);
        return max;
    }

    /**
     * Remove all non alphabet character
     */
    public String removeAllNonAlphabets(String s) {
        return s.replaceAll("[^a-zA-Z]","");
    }

    private static final Logger logger = LogManager.getLogger(Utilities.class);
}

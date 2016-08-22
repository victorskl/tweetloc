package com.sankholin.comp90049.project1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public final class StringUtilities {

    private static StringUtilities instance = null;
    private StringUtilities() {}
    static synchronized StringUtilities getInstance() {
        if (instance == null) instance = new StringUtilities();
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

    private static final Logger logger = LogManager.getLogger(StringUtilities.class);
}

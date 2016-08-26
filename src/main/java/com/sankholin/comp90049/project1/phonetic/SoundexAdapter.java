package com.sankholin.comp90049.project1.phonetic;

import com.sankholin.comp90049.project1.StringSearch;
import com.sankholin.comp90049.project1.tool.Utilities;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.Soundex;

public class SoundexAdapter implements StringSearch {

    private Soundex soundex = SoundexSingleton.getInstance();
    private Utilities util = Utilities.getInstance();

    @Override
    public int getScore(String s1, String s2) {
        s1 = util.removeAllNonAlphabets(s1);
        s2 = util.removeAllNonAlphabets(s2);

        try {
            return soundex.difference(s1, s2);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        return Integer.MIN_VALUE;
    }

    public String encode(String s) {
        s = util.removeAllNonAlphabets(s);
        return soundex.encode(s);
    }
}

class SoundexSingleton {
    private static Soundex instance = null;
    private SoundexSingleton() {}
    static synchronized Soundex getInstance() {
        if (instance == null) instance = new Soundex();
        return instance;
    }
}

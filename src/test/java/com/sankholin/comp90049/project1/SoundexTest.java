package com.sankholin.comp90049.project1;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.Soundex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SoundexTest {

    @Test
    public void testTranslationTable() throws EncoderException {
        // using Apache common codec
        Soundex soundex = new Soundex();
        assertEquals("K520", soundex.encode("king"));
        assertEquals("K520", soundex.encode("kyngge"));
        assertEquals(4, soundex.difference("king", "kyngge"));
        assertEquals(true, soundex.difference("ding", "kyngge")<4);
    }
}

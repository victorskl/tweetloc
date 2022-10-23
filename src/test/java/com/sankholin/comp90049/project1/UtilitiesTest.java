package com.sankholin.comp90049.project1;

import com.sankholin.comp90049.project1.tool.Utilities;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.Test;

import java.util.List;

import static org.apache.lucene.analysis.classic.ClassicAnalyzer.STOP_WORDS_SET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class UtilitiesTest {

    private final Utilities util = Utilities.getInstance();
    private final StandardAnalyzer standardAnalyzer = new StandardAnalyzer(STOP_WORDS_SET);

    @Test
    public void testSingleton() {
        Utilities util2 = Utilities.getInstance();
        assertSame(util, util2);
    }

    @Test
    public void testTokenizeString() {
        String str = "@tweet umm... this is - a test 0.";
        List<String> stringList = util.tokenizeString(standardAnalyzer, str);
        assertEquals("tweet", stringList.get(0));
        assertEquals("umm", stringList.get(1));
        assertEquals("test", stringList.get(2));
        assertEquals("0", stringList.get(3));
        assertEquals("tweet umm test 0", String.join(" ", stringList));
    }

    @Test
    public void testMax() {
        assertEquals(2, util.max(1, 2));
        assertEquals(4, util.max(1, 2, 3, 4));
        assertEquals(100, util.max(-1, 2, 3, 100));
    }

    @Test
    public void testMin() {
        assertEquals(1, util.min(1, 2));
        assertEquals(1, util.min(1, 2, 3, 4));
        assertEquals(-1, util.min(-1, 2, 3, 100));
    }
}

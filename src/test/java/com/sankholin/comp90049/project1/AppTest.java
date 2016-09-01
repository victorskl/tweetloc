package com.sankholin.comp90049.project1;

import com.sankholin.comp90049.project1.editdistance.GlobalEditDistance;
import com.sankholin.comp90049.project1.editdistance.LocalEditDistance;
import com.sankholin.comp90049.project1.editdistance.NGramDistance;
import com.sankholin.comp90049.project1.phonetic.SoundexAdapter;
import com.sankholin.comp90049.project1.tool.GazetteerAnalyzer;
import com.sankholin.comp90049.project1.tool.Utilities;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppTest {

    private StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
    private SimpleAnalyzer simpleAnalyzer = new SimpleAnalyzer();
    private GazetteerAnalyzer gazetteerAnalyzer = new GazetteerAnalyzer();

    private Utilities util = Utilities.getInstance();
    private File configFile = new File("./config.properties");
    private Configuration config;
    private File gazetteerFile;
    private File tweetsFile;

    @Before
    public void before() {
        Configurations configs = new Configurations();
        try {
            config = configs.properties(configFile);
            gazetteerFile = new File(config.getString("gazetteer"));
            tweetsFile = new File(config.getString("tweets"));
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Test @Ignore
    public void observeTweet() {
        try {

            List<String> tweets = FileUtils.readLines(tweetsFile, "UTF-8");
            System.out.println(tweets.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test @Ignore
    public void observeGazetteer() {
        try {

            List<String> gazetteer = FileUtils.readLines(gazetteerFile, "UTF-8");

            int idx=1, cnt=0;
            for (String aGazetteer : gazetteer) {
                List<String> aGazetteerTokenList = util.tokenizeString(gazetteerAnalyzer, aGazetteer);
                String aFilteredGazetteer = String.join(" ", aGazetteerTokenList);
                int chunkSize = aFilteredGazetteer.length();
                if (chunkSize < 3) { //location name with less than x characters
                    System.out.println(aGazetteer + "\t\t\t[" + aFilteredGazetteer + "]\t\t\t (index: " + idx + ")");
                    cnt++;
                }
                idx++;
            }
            System.out.println("count: " + cnt);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test @Ignore
    public void howItLookLike() {
        String str = "Sanford-Springvale Chamber of Commerce";
        //str = "San Francisco Chinese Parent Committee School (historical)";
        //str = "San Francisco Banco Number 60";
        //str = "37N03W33BC__01 Well";
        //str = "40 B Water Well";
        //str = "3C - Capehart Communication Collection Building";
        //str = "519 / Wrigley Volunteer Fire Department";
        //str = "\"Alson H Smith, Junior Library\"";
        str = "“Rebecca of the Well” Fountain";
        //str = "A-8 Water Well";
        //str = "A 9.4 Lateral";
        //str = "89'er Museum Park";
        //str = "3-M's Airport";
        //str ="Y";
        //str = "T T Well";
        str = "A and K";
        //str = "@amandapalmer http://twitpic.com/dbfan - This rules!";
        //str = "@trapped it's really good!!! It is made by a brewery in San Fransisco... It has a hint of watermelon but it is not over powering!!";
        //str = "If a relationship has to be a secret, you shouldn't be in it.";
        List<String> stringList = null;

        //stringList = util.tokenizeString(standardAnalyzer, str);
        //stringList = util.tokenizeString(simpleAnalyzer, str);
        stringList = util.tokenizeString(gazetteerAnalyzer, str);

        String s = String.join(" ", stringList);
        System.out.println(s);

        System.out.println();
        System.out.println("[Regex - only Alphabets - retain space]");
        s = s.replaceAll("[^a-zA-Z ]","");
        System.out.println(s);
        System.out.println("[Regex - only Alphabets]");
        s = s.replaceAll("[^a-zA-Z]","");
        System.out.println(s);
    }

    @Test @Ignore
    public void testChunk() {
        GlobalEditDistance ged = new GlobalEditDistance();
        ged.setMatch(0);
        ged.setReplace(1);
        ged.setDeletion(1);
        ged.setInsertion(1);

        LocalEditDistance led = new LocalEditDistance();
        led.setMatch(1);
        led.setReplace(-1);
        led.setDeletion(-1);
        led.setInsertion(-1);

        NGramDistance ngm = new NGramDistance();

        SoundexAdapter sdx = new SoundexAdapter();

        String tweet = "@trapped it's really good!!! It is made by a brewery in San Fransisco... It has a hint of watermelon but it is not over powering!!";
        List<String> tweetTokens = util.tokenizeString(standardAnalyzer, tweet);
        tweet = String.join(" ", tweetTokens);
        System.out.println(tweet);

        List<String> locations = new ArrayList<>();
        //locations.add(" ");
        //locations.add("San Francisco Pass");
        locations.add("San Francisco");
        //locations.add("Sahm Park");
        //locations.add("R E A Canyon");
        //locations.add("3-H Lake");
        //locations.add("58 133 F");
        //locations.add("B58-34-2");
        //locations.add("Pa-455");
        //locations.add("Ex");
        //locations.add("4th");
        //locations.add("Oz");
        //locations.add("M-6");
        //locations.add("Y");

        int tweetLength = tweet.length();
        System.out.println("tweetLength: " +tweetLength);
        System.out.println();

        System.out.println("....::MULTI-WORD::....");

        for (String aTweetToken : tweetTokens) {
            for (String location : locations) {
                List<String> locationToken = util.tokenizeString(gazetteerAnalyzer, location);
                location = String.join(" ", locationToken);
                int chunkSize = location.length();

                //System.out.println("chunkSize: " +chunkSize);

                //System.out.println(tweet.indexOf(aTweetToken));
                int head = tweet.indexOf(aTweetToken);
                int tail = head + chunkSize;
                if (tail > tweetLength) {
                    tail = tweetLength;
                }
                String term = tweet.substring(head, tail);
/*
                System.out.println("head: " + head + "\t" + term + "\t" + location
                        + "\t" + ged.getScore(term, location)
                        + "\t" + sdx.getScore(term, location)
                        //+ "\t" + ged.getScore(sdx.encode(term), sdx.encode(location))
                        + "\t" + ngm.getScore(term, location)
                );
*/

                System.out.println(term
                        + "\t" + location
                        + "\t" + ged.getScore(term, location)
                        + "\t" + ngm.getScore(term, location)
                        + "\t" + sdx.getScore(term, location)
                        + "\t" + ged.getScore(sdx.encode(term), sdx.encode(location))
                );

            }
        }

        System.out.println();
        System.out.println("....::SINGLE-WORD::....");

        for (String aTweetToken : tweetTokens) {
            for (String location : locations) {

                location = String.join(" ", util.tokenizeString(gazetteerAnalyzer, location)); //preprocessed

                System.out.println(aTweetToken
                        + "\t" + location
                        + "\t" + ged.getScore(aTweetToken, location)
                        + "\t" + ngm.getScore(aTweetToken, location)
                        + "\t" + sdx.getScore(aTweetToken, location)
                        + "\t" + ged.getScore(sdx.encode(aTweetToken), sdx.encode(location))
                );
            }
        }

        System.out.println();
        System.out.println("....::LED::....");

        for (String location : locations) {
            location = String.join(" ", util.tokenizeString(gazetteerAnalyzer, location));
            System.out.println(tweet + "\t" + location + "\t" + led.getScore(tweet, location));
        }
    }
}

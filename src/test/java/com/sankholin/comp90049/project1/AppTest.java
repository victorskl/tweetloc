package com.sankholin.comp90049.project1;

import com.sankholin.comp90049.project1.editdistance.GlobalEditDistance;
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
import java.util.Collections;
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
                List<String> aGazetteerTokenList = util.tokenizeString(simpleAnalyzer, aGazetteer);
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
    public void reprocessGazetteer() {
        try {

            List<String> gazetteer = FileUtils.readLines(gazetteerFile, "UTF-8");
            System.out.println(gazetteer.size()); // size

            System.out.println(gazetteer.get(1)); // head
            System.out.println(gazetteer.get(gazetteer.size()/2)); // mid
            System.out.println(gazetteer.get(gazetteer.size()-1)); // tail

            //gazetteer is sorted but make sure we sort it again
            Collections.sort(gazetteer);

            System.out.println("\nAfter sort...\n");

            // check again
            System.out.println(gazetteer.get(0));
            System.out.println(gazetteer.get(1)); // head
            System.out.println(gazetteer.get(2));
            System.out.println(gazetteer.get(3));
            System.out.println(gazetteer.get(4));
            System.out.println(gazetteer.get(5));
            System.out.println(gazetteer.get(gazetteer.size()/2)); // mid
            System.out.println(gazetteer.get(gazetteer.size()-2));
            System.out.println(gazetteer.get(gazetteer.size()-1)); // tail

            // "Alson H Smith, Junior Library"
            // “Rebecca of the Well” Fountain

            System.out.println("\n");

            // try to re-process the way we want

            List<String> gazetteerNew = new ArrayList<>();

            for (String s : gazetteer) {
                s = String.join(" ", util.tokenizeString(gazetteerAnalyzer, s));
                gazetteerNew.add(s);
            }

            Collections.sort(gazetteer);

            System.out.println(gazetteerNew.get(0));
            System.out.println(gazetteerNew.get(1)); // head
            System.out.println(gazetteerNew.get(2));
            System.out.println(gazetteerNew.get(3));
            System.out.println(gazetteerNew.get(4));
            System.out.println(gazetteerNew.get(5));
            System.out.println(gazetteerNew.get(gazetteer.size()/2)); // mid
            System.out.println(gazetteerNew.get(gazetteer.size()-2));
            System.out.println(gazetteerNew.get(gazetteer.size()-1)); // tail

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test @Ignore
    public void howItLookLike() {
        String str = "Sanford-Springvale Chamber of Commerce";
        str = "San Francisco Chinese Parent Committee School (historical)";
        str = "San Francisco Banco Number 60";
        str = "37N03W33BC__01 Well";
        //str = "40 B Water Well";
        //str = "3C - Capehart Communication Collection Building";
        //str = "519 / Wrigley Volunteer Fire Department";
        //str = "\"Alson H Smith, Junior Library\"";
        //str = "“Rebecca of the Well” Fountain";
        //str = "A-8 Water Well";
        //str = "A 9.4 Lateral";
        //str = "89'er Museum Park";
        //str = "3-M's Airport";
        //str ="Y";
        //str = "T T Well";
        //str = "A and K";
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
        ged.setMatch(1);
        ged.setReplace(-1);
        ged.setDeletion(-1);
        ged.setInsertion(-1);

        SoundexAdapter soundexAdapter = new SoundexAdapter();

        String tweet = "@trapped it's really good!!! It is made by a brewery in San Fransisco... It has a hint of watermelon but it is not over powering!!";
        List<String> tweetTokens = util.tokenizeString(standardAnalyzer, tweet);
        tweet = String.join(" ", tweetTokens);
        System.out.println(tweet);

        List<String> locations = new ArrayList<>();
        //locations.add(" ");
        locations.add("San Francisco Pass");
        //locations.add("San Francisco");
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

        for (String aTweetToken : tweetTokens) {
            for (String location : locations) {
                List<String> locationToken = util.tokenizeString(simpleAnalyzer, location);
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
                //System.out.println("head: " + head + "\t" + term + "\t" + location + "\t" + ged.getScore(term, location)
                //        + "\t" + soundexAdapter.getScore(term, location) + "\t" +soundexAdapter.encode(location));

                //System.out.println();
                System.out.println("head: " + head + "\t" + term + "\t" + location + "\t"
                        + ged.getScore(soundexAdapter.encode(term), soundexAdapter.encode(location)));
            }
        }
    }
}

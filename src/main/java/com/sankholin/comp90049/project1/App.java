package com.sankholin.comp90049.project1;

import com.opencsv.CSVWriter;
import com.sankholin.comp90049.project1.editdistance.LevenshteinDistance;
import com.sankholin.comp90049.project1.editdistance.NGramDistance;
import com.sankholin.comp90049.project1.editdistance.NWEditDistance;
import com.sankholin.comp90049.project1.editdistance.StringDistance;
import com.sankholin.comp90049.project1.model.ResultModel;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class App {

    @Option(name = "-a", usage = "lv=Levenshtein,nw=Needleman–Wunsch,ng=NGram")
    private String algorithm = "lv";

    @Option(name = "-d", usage = "Dryrun for first few lines up to d")
    private int dryrun = 0;

    @Option(name = "-o", usage = "output to this file", metaVar = "OUTPUT")
    private File out = new File("output.csv");

    private CSVWriter writer;
    private StandardAnalyzer standardAnalyzer;
    private List<String> tweets;
    private List<String> gazetteer;
    private StringDistance stringDistance;

    public static void main(String[] args) {
        new App().doMain(args);
    }

    private void doMain(String[] args) {
        File configFile = new File("./config.properties");
        logger.info("Reading config file: " +configFile.toString());
        Configurations configs = new Configurations();
        CmdLineParser parser = new CmdLineParser(this);

        try {

            logger.info("Parsing args");
            parser.parseArgument(args);
            logger.info("option: -d " +dryrun);
            logger.info("option: -a " +algorithm);
            logger.info("option: -o " +out.toString());

            Configuration config = configs.properties(configFile);
            logger.info("Tweet collection: " +config.getString("tweets"));
            logger.info("Gazetteer: " +config.getString("gazetteer"));

            int minNGramLength = config.getInt("minngramlength");
            int maxNGramLength = config.getInt("maxngramlength");

            standardAnalyzer = new StandardAnalyzer();

            //setup LevenshteinDistance
            if (algorithm.equalsIgnoreCase("lv")) {
                LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
                levenshteinDistance.setMatch(config.getInt("lv.match"));
                levenshteinDistance.setInsertion(config.getInt("lv.insertion"));
                levenshteinDistance.setDeletion(config.getInt("lv.deletion"));
                levenshteinDistance.setReplace(config.getInt("lv.replace"));
                levenshteinDistance.printScore();
                stringDistance = levenshteinDistance;
            }

            //setup NWEditDistance
            if (algorithm.equalsIgnoreCase("nw")) {
                NWEditDistance nwEditDistance = new NWEditDistance();
                nwEditDistance.setMatch(config.getInt("nw.match"));
                nwEditDistance.setInsertion(config.getInt("nw.insertion"));
                nwEditDistance.setDeletion(config.getInt("nw.deletion"));
                nwEditDistance.setReplace(config.getInt("nw.replace"));
                nwEditDistance.printScore();
                stringDistance = nwEditDistance;
            }

            //setup NGramDistance
            if (algorithm.equalsIgnoreCase("ng")) {
                NGramDistance nGramDistance = new NGramDistance();
                nGramDistance.setMinNGramLength(minNGramLength);
                nGramDistance.setMaxNGramLength(maxNGramLength);
                stringDistance = nGramDistance;
            }

            File tweetsFile = new File(config.getString("tweets"));
            tweets = FileUtils.readLines(tweetsFile, "UTF-8");

            File gazetteerFile = new File(config.getString("gazetteer"));
            gazetteer = FileUtils.readLines(gazetteerFile, "UTF-8");

            writer = new CSVWriter(new FileWriter(out));
            String[] titleText = {"Tweet UserId", " Tweet Id", "Tweet Text", "Tweet Timestamp", "Match Term", "Location", "Score"};
            writer.writeNext(titleText);

            start();

        } catch (IOException | ConfigurationException | CmdLineException e) {
            e.printStackTrace();
        }
    }

    private void start() throws IOException {
        int idx = 0;
        for (String tweet : tweets) {
            if (dryrun > 0 && idx == dryrun) break; //just test first few lines

            String[] aRawTweet = tweet.split("\\t");
            if (aRawTweet.length != 4) {
                String msg = "Tweet anomaly at index: [" + idx + "] \t\t" + tweet;
                logger.warn(msg);
                FileUtils.writeStringToFile(new File("tweet_anomaly.log"), msg, "UTF-8");
                idx++;
                continue;
            }

            //will only process tweet_text column
            String tweetText = aRawTweet[2];
            logger.info("Processing tweet [" + idx + "] " + tweetText);

            ResultModel resultModel = new ResultModel();
            resultModel.setTweetUserId(aRawTweet[0]);
            resultModel.setTweetId(aRawTweet[1]);
            resultModel.setTweetText(tweetText);
            resultModel.setTweetTimestamp(aRawTweet[3]);

            //tokenize using Lucene StandardAnalyzer
            List<String> tokenizeTweets = StringUtilities.getInstance().tokenizeString(standardAnalyzer, tweetText);

            // threshold for comparing best match among tokens
            int threshold = Integer.MAX_VALUE;
            if (algorithm.equalsIgnoreCase("nw")) {
                threshold = Integer.MIN_VALUE;
            }

            // for each tweet token
            for (String tweetToken : tokenizeTweets) {

                // compare against dictionary for misspelled location
                for (String aGazetteer : gazetteer) {

                    //we will skip a blank dictionary entry, not meaningful for finding approximate match
                    if (StringUtils.isBlank(aGazetteer)) continue;

                    //won't tokenize but toLowerCase
                    aGazetteer = aGazetteer.toLowerCase();

                    // Commons Lang3 package has LevenshteinDistance implementation..
                    // But will use our implementation. Anchor here for just to compare with our implementation.
                    //int ged = StringUtils.getLevenshteinDistance(tweetToken, aGazetteer);

                    int ged = stringDistance.getDistance(tweetToken, aGazetteer);

                    if (algorithm.equalsIgnoreCase("nw")) {
                        // Needleman–Wunsch algorithm: The lower the alignment score the larger the edit distance
                        // therefore look for bigger score.
                        if (ged > threshold) {
                            threshold = ged;
                            resultModel.setScore(threshold);
                            resultModel.setGazetteer(aGazetteer);
                            resultModel.setTweetToken(tweetToken);
                        }
                    } else {
                        // NGram and Levenshtein Distance: A higher score indicates a greater distance
                        // therefore look for smaller score.
                        if (ged < threshold) { //FIXME 0 is exact match, but we are looking for misspelled? also do (.. && ged > 0)?
                            threshold = ged;
                            resultModel.setScore(threshold);
                            resultModel.setGazetteer(aGazetteer);
                            resultModel.setTweetToken(tweetToken);
                        }
                    }
                }
            }

            String[] entries = {
                    resultModel.getTweetUserId(),
                    resultModel.getTweetId(),
                    resultModel.getTweetText(),
                    resultModel.getTweetTimestamp(),
                    resultModel.getTweetToken(),
                    resultModel.getGazetteer(),
                    "" + resultModel.getScore()
            };
            writer.writeNext(entries);
            idx++;
        }

        writer.close();
        logger.info("DONE!");
    }

    private static final Logger logger = LogManager.getLogger(App.class);
}

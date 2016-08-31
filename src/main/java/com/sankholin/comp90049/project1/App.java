package com.sankholin.comp90049.project1;

import com.opencsv.CSVWriter;
import com.sankholin.comp90049.project1.editdistance.GlobalEditDistance;
import com.sankholin.comp90049.project1.editdistance.LocalEditDistance;
import com.sankholin.comp90049.project1.editdistance.NGramDistance;
import com.sankholin.comp90049.project1.model.MatchTermCandidate;
import com.sankholin.comp90049.project1.neighbourhood.AgrepWrapper;
import com.sankholin.comp90049.project1.phonetic.SoundexAdapter;
import com.sankholin.comp90049.project1.tool.Utilities;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class App {

    @Option(name = "-a", usage = "ged=Global Edit Distance,led=Local Edit Distance,ngm=NGram,sdx=Soundex,nbh=Neighbourhood Search")
    private String algorithm = "ged";

    @Option(name = "--single", usage = "Single Word Location, ignore if '-a led'")
    private boolean single = false;

    @Option(name = "-d", usage = "Dryrun for first few lines up to d")
    private int dryRun = 0;

    @Option(name = "-i", usage = "Start index of Tweet")
    private int startIdx = 0;

    @Option(name = "-o", usage = "output to this file", metaVar = "OUTPUT")
    private File out = new File("output.csv");

    @Option(name = "-c", usage = "config file")
    private File configFile = new File("./config.properties");

    @Option(name = "-xx", usage = "xx=Lower limit of score")
    private Integer lowerLimit = null;

    @Option(name = "-zz", usage = "zz=Upper limit of score")
    private Integer upperLimit = null;

    @Option(name = "--preprocess", usage = "parted=Partition Tweets,gaze=Re-process Gazetteer")
    private String preProcess = null;

    private CSVWriter writer;
    private List<String> tweets;
    private List<String> gazetteer;
    private StringSearch stringSearch;
    private Utilities util = Utilities.getInstance();
    private boolean isTokenize = true;
    private boolean isMinimalScore;
    private int candidateLimit = 1;
    //private int minCharLocationName = 2;

    private StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
    //private GazetteerAnalyzer gazetteerAnalyzer = new GazetteerAnalyzer();

    public static void main(String[] args) {
        new App().doMain(args);
    }

    private void doMain(String[] args) {
        try {
            CmdLineParser parser = new CmdLineParser(this);
            logger.info("Parsing args...");
            parser.parseArgument(args);

            logger.info("option: -c " + configFile.toString());
            logger.info("Reading config file: " +configFile.toString());
            Configurations configs = new Configurations();
            Configuration config = configs.properties(configFile);
            logger.info("Tweet collection: " +config.getString("tweets"));
            logger.info("Gazetteer: " +config.getString("gazetteer.preprocessed"));

            if (preProcess != null) {
                logger.info("option: --preprocess " + preProcess);
                logger.warn("Redirect to pre-processing steps. Init...");
                PreProcessor processor = new PreProcessor(config);
                if (preProcess.equalsIgnoreCase("parted")) {
                    logger.warn("Tweet partition option is given. Please re-invoke application after partition has done.");
                    processor.partitionTweetFile();
                    logger.info("DONE!");
                    logger.info("Tweet partition files are saved under " + config.getString("tweets.partition.dir"));
                    logger.info("You might want to point to the new parted tweet file before next invocation.");
                    logger.info("\te.g. at config.properties, change 'tweets=res/tweets/xxx_tweets_0.txt'");
                    logger.info("Please re-run application without '--preprocess parted' option to continue main routine.");
                    return;
                }
                else if (preProcess.equalsIgnoreCase("gaze")) {
                    processor.reprocessGazetteer();
                    logger.info("Gazetteer pre-processing has done. Continue with main routine...");
                }
                else {
                    logger.error("Unrecognized PreProcessor argument. '--preprocess " + processor + "'");
                }
            }

            logger.info("option: -d " + dryRun);
            logger.info("option: -i " + startIdx);
            logger.info("option: -a " + algorithm);
            logger.info("option: -o " + out.toString());

            if (lowerLimit != null) {
                logger.info("option: -xx " + lowerLimit);
                logger.warn("Lower limit option [-xx] is only applied for m < i,d,r and NGram.");
            }

            if (upperLimit != null) {
                logger.info("option: -zz " + upperLimit);
                logger.warn("Upper limit option [-zz] is only applied for m > i,d,r and Soundex.");
            }

            //

            int minNGramLength = config.getInt("minngramlength");
            int maxNGramLength = config.getInt("maxngramlength");
            candidateLimit = config.getInt("candidatelimit");
            if (candidateLimit < 1) candidateLimit = 1;
            //minCharLocationName = config.getInt("mincharlocationname");

            // Default to true for GED and LED (m < i,d,r) and NGram
            isMinimalScore = true;

            File tweetsFile = new File(config.getString("tweets"));

            File gazetteerFile = new File(config.getString("gazetteer.preprocessed"));
            if (!gazetteerFile.canRead()) {
                logger.error("Preprocessed gazetteer file is not found. Please run with option '--preprocess gaze'");
                logger.error("This Gazetteer preprocessing is only required to be done once.");
            }

            //setup GlobalEditDistance
            if (algorithm.equalsIgnoreCase("ged")) {
                GlobalEditDistance globalEditDistance = new GlobalEditDistance();
                globalEditDistance.setMatch(config.getInt("ged.match"));
                globalEditDistance.setInsertion(config.getInt("ged.insertion"));
                globalEditDistance.setDeletion(config.getInt("ged.deletion"));
                globalEditDistance.setReplace(config.getInt("ged.replace"));
                globalEditDistance.printScore();
                isMinimalScore = globalEditDistance.isMinimalScore();
                stringSearch = globalEditDistance;
            }

            //setup LocalEditDistance
            if (algorithm.equalsIgnoreCase("led")) {
                isTokenize = false;
                LocalEditDistance localEditDistance = new LocalEditDistance();
                localEditDistance.setMatch(config.getInt("led.match"));
                localEditDistance.setInsertion(config.getInt("led.insertion"));
                localEditDistance.setDeletion(config.getInt("led.deletion"));
                localEditDistance.setReplace(config.getInt("led.replace"));
                localEditDistance.printScore();
                isMinimalScore = localEditDistance.isMinimalScore();
                stringSearch = localEditDistance;
                candidateLimit = 1; // LED only has one candidate to output
            }

            //setup NGramDistance
            if (algorithm.equalsIgnoreCase("ngm")) {
                NGramDistance nGramDistance = new NGramDistance();
                if (maxNGramLength < minNGramLength) {
                    logger.error("NGram max length is smaller than min. " + maxNGramLength + " > " + minNGramLength + ". Halt!");
                    return;
                }
                nGramDistance.setMinNGramLength(minNGramLength);
                nGramDistance.setMaxNGramLength(maxNGramLength);
                stringSearch = nGramDistance;
            }

            //step SoundexAdapter
            if (algorithm.equalsIgnoreCase("sdx")) {
                SoundexAdapter soundexAdapter = new SoundexAdapter();
                isMinimalScore = false; // based on org.apache.commons.codec.language.Soundex.difference()
                stringSearch = soundexAdapter;
            }

            if (algorithm.equalsIgnoreCase("nbh")) {

                new AgrepWrapper(config, tweetsFile, gazetteerFile, startIdx, dryRun, out, candidateLimit);

                // The rest of the operation redirect to AgrepWrapper and exit
                // Line below won't be required executing anymore
                return;
            }

            if (stringSearch == null) {
                logger.error("Unrecognized algorithm option '-a " + algorithm  + "'. Halt!");
                return;
            }

            // try as lazy as possible
            tweets = FileUtils.readLines(tweetsFile, "UTF-8");
            gazetteer = FileUtils.readLines(gazetteerFile, "UTF-8");

            if (single && !algorithm.equalsIgnoreCase("led")) {
                logger.info("option: --single");
                logger.info("Processing in single-word gazetteer matching...");
            } else {
                logger.info("Processing in multi-word gazetteer matching...");
            }

            writer = new CSVWriter(new FileWriter(out));
            String[] titleText = new String[4+candidateLimit*3];
            titleText[0] = "Tweet UserId";
            titleText[1] = "Tweet Id";
            titleText[2] = "Tweet Text";
            titleText[3] = "Tweet Timestamp";
            for (int i=4; i < titleText.length;) {
                titleText[i] = "Location";
                titleText[i+1] = "Match Term";
                titleText[i+2] = "Score";
                i = i + 3;
            }
            writer.writeNext(titleText);

            start();

        } catch (IOException | ConfigurationException | CmdLineException e) {
            e.printStackTrace();
        }
    }

    private void start() throws IOException {
        if (startIdx < 0) startIdx = 0;
        if (startIdx > 0 && dryRun > 0) dryRun = dryRun + startIdx;

        for (int i = startIdx; i < tweets.size(); i++) {

            if (dryRun > 0 && i == dryRun) break; //just test first few lines

            String tweet = tweets.get(i);

            String[] aRawTweet = tweet.split("\\t");
            if (aRawTweet.length != 4) {
                String msg = "Tweet anomaly at index: [" + i + "] \t\t" + tweet;
                logger.warn(msg);
                continue;
            }
            logger.info("Processing tweet [" + i + "] " + aRawTweet[2]);
            process(aRawTweet);
        }

        writer.close();
        logger.info("DONE!");
    }

    private void process(String[] aRawTweet) {
        //will only process tweet_text column
        String tweetText = aRawTweet[2];

        List<String> output = new ArrayList<>();
        output.add(aRawTweet[0]);
        output.add(aRawTweet[1]);
        output.add(tweetText);
        output.add(aRawTweet[3]);

        List<String> tokenizeTweets = util.tokenizeString(standardAnalyzer, tweetText);

        //if GED and NG, process in tokens
        if (isTokenize) {
            List<MatchTermCandidate> candidateList;
            if (single) {
                candidateList = processInSingleWordLocation(tokenizeTweets);
            } else {
                candidateList = processInMultiWordLocation(tokenizeTweets);
            }

            if (isMinimalScore) {
                for (Object o : candidateList.stream().sorted().limit(candidateLimit).toArray()) {
                    MatchTermCandidate c = (MatchTermCandidate) o;
                    output.add(c.getGazetteer());
                    output.add(c.getTerm());
                    output.add(c.getScore()+"");
                }
            } else {
                for (Object o : candidateList.stream().sorted(Comparator.reverseOrder()).limit(candidateLimit).toArray()) {
                    MatchTermCandidate c = (MatchTermCandidate) o;
                    output.add(c.getGazetteer());
                    output.add(c.getTerm());
                    output.add(c.getScore()+"");
                }
            }

        } else {
            String filteredTweetText = String.join(" ", tokenizeTweets);
            MatchTermCandidate candidate = processInMonolithicString(filteredTweetText);
            output.add(candidate.getGazetteer());
            output.add(candidate.getTerm());
            output.add(candidate.getScore()+"");
        }

        writer.writeNext(output.stream().toArray(String[]::new));
    }

    private List<MatchTermCandidate> processInMultiWordLocation(List<String> tweetTokens) {
        List<MatchTermCandidate> candidateList = new ArrayList<>();

        String filteredTweetText = String.join(" ", tweetTokens);
        int tweetTextLength = filteredTweetText.length();

        for (String aTweetToken : tweetTokens) {
            int head = filteredTweetText.indexOf(aTweetToken);

            int[] threshold = {Integer.MAX_VALUE};
            if (!isMinimalScore) threshold[0] = Integer.MIN_VALUE;

            MatchTermCandidate candidate = new MatchTermCandidate();

            for (String aGazetteer : gazetteer) {
                //TODO PreProcessor
                //List<String> aGazetteerTokenList = util.tokenizeString(gazetteerAnalyzer, aGazetteer);
                //String aFilteredGazetteer = String.join(" ", aGazetteerTokenList);
                int chunkSize = aGazetteer.length();
                //if (chunkSize < minCharLocationName) continue;

                int tail = head + chunkSize;
                // location length is longer than tweet length
                if (tail > tweetTextLength) tail = tweetTextLength;
                String tweetChunk = filteredTweetText.substring(head, tail);

                int score = stringSearch.getScore(tweetChunk, aGazetteer);
                evaluateScore(score, threshold, candidate, aGazetteer, tweetChunk);
            }

            candidateList.add(candidate);
        }

        return candidateList;
    }

    private List<MatchTermCandidate> processInSingleWordLocation(List<String> tokenizeTweets) {
        List<MatchTermCandidate> candidateList = new ArrayList<>();

        // for each tweet token
        for (String tweetToken : tokenizeTweets) {

            int[] threshold = { Integer.MAX_VALUE };
            if (!isMinimalScore) threshold[0] = Integer.MIN_VALUE;

            // each token can be a potential MatchTermCandidate
            MatchTermCandidate candidate = new MatchTermCandidate();

            // compare against dictionary for misspelled location
            for (String aGazetteer : gazetteer) {
                //TODO PreProcessor
                //List<String> aGazetteerTokenList = util.tokenizeString(gazetteerAnalyzer, aGazetteer);
                //String aFilteredGazetteer = String.join(" ", aGazetteerTokenList);
                //if (aGazetteer.length() < minCharLocationName) continue;

                int score = stringSearch.getScore(tweetToken, aGazetteer);

                // evaluate score
                evaluateScore(score, threshold, candidate, aGazetteer, tweetToken);
            }

            candidateList.add(candidate);
        }

        return candidateList;
    }

    private MatchTermCandidate processInMonolithicString(String tweetText) {

        int[] threshold = { Integer.MAX_VALUE };
        if (!isMinimalScore) threshold[0] = Integer.MIN_VALUE;

        MatchTermCandidate candidate = new MatchTermCandidate();

        for (String aGazetteer : gazetteer) {
            //TODO PreProcessor
            //List<String> aGazetteerTokenList = util.tokenizeString(gazetteerAnalyzer, aGazetteer);
            //String aFilteredGazetteer = String.join(" ", aGazetteerTokenList);
            //if (aFilteredGazetteer.length() < minCharLocationName) continue;

            int score = stringSearch.getScore(tweetText, aGazetteer);

            evaluateScore(score, threshold, candidate, aGazetteer, tweetText);
        }

        return candidate;
    }

    /**
     * Each tweet token, pick the best match against dictionary.
     * TODO: how to deal with a tie breaker between locations?
     *
     * @param score a calculated score, of given algorithm
     * @param threshold a pointer, previous best score
     * @param candidate the best match candidate - of best match pair C(tweet, gazetteer)
     * @param aGazetteer a location to evaluate
     * @param tweetTerm a matching term
     */
    private void evaluateScore(int score, int[] threshold, MatchTermCandidate candidate, String aGazetteer, String tweetTerm) {

        // For m < i,d,r and NGram
        if (isMinimalScore) {

            // Not an ideal condition.
            // But for this project, we look more for a misspelled word, i.e. exact matching is not so much
            if (lowerLimit != null && score < lowerLimit) return;

            if (score < threshold[0]) {
                threshold[0] = score;
                candidate.setGazetteer(aGazetteer);
                candidate.setTerm(tweetTerm);
                candidate.setScore(score);
            }
        } else {

            // Not an ideal condition.
            // But for this project, we look more for a misspelled word, i.e. exact matching is not so much
            if (upperLimit != null && score > upperLimit) return;

            // For m > i,d,r and Soundex
            if (score > threshold[0]) {
                threshold[0] = score;
                candidate.setGazetteer(aGazetteer);
                candidate.setTerm(tweetTerm);
                candidate.setScore(score);
            }
        }
    }

    private static final Logger logger = LogManager.getLogger(App.class);
}

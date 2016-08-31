package com.sankholin.comp90049.project1.neighbourhood;

import com.opencsv.CSVWriter;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AgrepWrapper {

    private File tweetsFile;
    private File gazetteerFile;
    private int startIdx;
    private int dryRun;
    private File out;
    private int candidateLimit;

    private String agrepPath;
    private int agrepError = 1;
    private String agrepOptions = "i";

    private MultiValuedMap<String, String> result;

    public AgrepWrapper(Configuration config, File tweetsFile, File gazetteerFile, int startIdx, int dryRun, File out, int candidateLimit) throws IOException {
        this.tweetsFile = tweetsFile;
        this.gazetteerFile = gazetteerFile;
        this.startIdx = startIdx;
        this.dryRun = dryRun;
        this.out = out;
        this.candidateLimit = candidateLimit;

        this.agrepPath = config.getString("cmd.agrep");
        this.agrepError = config.getInt("cmd.agrep.error");
        this.agrepOptions = config.getString("cmd.agrep.options");

        logger.info("Agrep path:    " +agrepPath);
        logger.info("Agrep error:   " +agrepError);
        logger.info("Agrep options: " +agrepOptions);

        result = new ArrayListValuedHashMap<>();
        start();
    }

    private void start() throws IOException {
        if (startIdx < 0) startIdx = 0;
        if (startIdx > 0 && dryRun > 0) dryRun = dryRun + startIdx;

        // inverse searching
        List<String> gazetteer = FileUtils.readLines(gazetteerFile, "UTF-8");
        logger.info("Running : " + agrepPath + " -" + agrepError + " -" + agrepOptions + " {dict} " + tweetsFile.getPath());
        String anim = "|/-\\";
        for (int i = startIdx; i < gazetteer.size(); i++) {
            if (dryRun > 0 && i == dryRun) break; //just test first few lines
            doAgrep(gazetteer.get(i), i);
            String data = "\r" + anim.charAt(i % anim.length())  + " " + i;
            System.out.write(data.getBytes());
        }
        System.out.println();

        // working on result
        CSVWriter writer = new CSVWriter(new FileWriter(out));
        String[] titleText = new String[4+candidateLimit];
        titleText[0] = "Tweet UserId";
        titleText[1] = "Tweet Id";
        titleText[2] = "Tweet Text";
        titleText[3] = "Tweet Timestamp";
        for (int i=4; i < titleText.length; i++) {
            titleText[i] = "Location";
        }
        writer.writeNext(titleText);

        List<String> tweets = FileUtils.readLines(tweetsFile, "UTF-8");
        for (String tweet : tweets) {
            String[] twee = tweet.split("\t");
            // to align with other result
            if (twee.length < 4 || twee.length > 4 || twee[0].isEmpty()) continue;

            if (result.containsKey(tweet)) {
                writer.writeNext((String[]) ArrayUtils.addAll(twee, result.get(tweet).stream().limit(candidateLimit).toArray()));
            } else {
                writer.writeNext(twee);
            }
        }
        writer.flush();
        writer.close();
        logger.info("DONE!");
    }

    private void doAgrep(String term, int idx) {
        try {
            List<String> commands = new ArrayList<>();
            commands.add(agrepPath);
            commands.add("-" + agrepError);
            commands.add("-" + agrepOptions);
            commands.add(term);
            commands.add(tweetsFile.getPath());

            ProcessBuilder builder = new ProcessBuilder();
            builder.redirectErrorStream(true);
            builder.command(commands);

            Process p = builder.start();
            //logger.trace("Running: " + Arrays.toString(commands.toArray()));

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                //if (line.equalsIgnoreCase("Grand Total: 0 match(es) found.")) continue;
                result.put(line, term);
            }

            int exitValue = p.waitFor();
            //System.out.println("\n\nExit with: " + exitValue);

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } catch (IllegalThreadStateException ie) {
            System.out.println();
            logger.warn("IllegalThreadStateException at dictionary index: " + idx + ", bug: JDK-8042019, action: continue...");
        }
    }

    private static final Logger logger = LogManager.getLogger(AgrepWrapper.class);
}

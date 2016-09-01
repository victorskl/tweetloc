package com.sankholin.comp90049.project1;

import com.sankholin.comp90049.project1.tool.Utilities;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AgrepTest {

    private File configFile = new File("./config.properties");
    private Configuration config;
    private File tweetsFile;
    private File gazetteerFile;
    private Utilities util = Utilities.getInstance();

    private List<String> tweets;
    private List<String> gazetteer;
    private String agrepPath;

    @Before
    public void before() {
        Configurations configs = new Configurations();
        try {
            config = configs.properties(configFile);
            tweetsFile = new File(config.getString("tweets"));
            tweets = FileUtils.readLines(tweetsFile, "UTF-8");
            gazetteerFile = new File(config.getString("gazetteer.preprocessed"));
            gazetteer = FileUtils.readLines(gazetteerFile, "UTF-8");

            logger.info("Tweet collection: " +config.getString("tweets"));
            logger.info("Gazetteer: " +config.getString("gazetteer"));

            logger.info("Agrep path: " +config.getString("cmd.agrep"));

            agrepPath = config.getString("cmd.agrep");

        } catch (ConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    MultiValuedMap<String, String> result;

    @Test @Ignore
    public void testAgrep() {
        result = new ArrayListValuedHashMap<>();

        //for (String aGazetteer : gazetteer) {
        //    printAgrepOut(aGazetteer);
        //}

        printAgrepOut("san francisco");
        printAgrepOut("burt");
        printAgrepOut("bute");
        printAgrepOut("c b");
        printAgrepOut("cad");
        printAgrepOut("cyr");


        System.out.println();

        for (String tweet : tweets) {
            if (result.containsKey(tweet)) {
                System.out.println(tweet + "\t\t\t" + Arrays.toString(result.get(tweet).toArray()));
            } else {
                System.out.println(tweet + "\t\t\t" + "NONE");
            }
        }
    }

    private void printAgrepOut(String term) {
        try {
            List<String> commands = new ArrayList<>();
            commands.add(agrepPath);
            commands.add("-1");
            commands.add("-iV0");
            commands.add(term);
            commands.add(tweetsFile.getPath());

            ProcessBuilder builder = new ProcessBuilder();
            builder.redirectErrorStream(true);
            builder.command(commands);

            Process p = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            logger.info("Running : " + Arrays.toString(commands.toArray()));
            while ((line = reader.readLine()) != null) {
                //if (line.equalsIgnoreCase("Grand Total: 0 match(es) found.")) continue;
                //System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()) + " " + term);
                //System.out.println(line.concat("\t").concat(term));
                result.put(line, term);
            }

            int exitValue = p.waitFor();
            //System.out.println("\n\nExit with: " + exitValue);

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } catch (IllegalThreadStateException ie) {
            System.out.println();
            logger.warn("IllegalThreadStateException at dictionary: " + term + ", bug: JDK-8042019, action: continue...");
        }
    }

    private static final Logger logger = LogManager.getLogger(AgrepTest.class);
}

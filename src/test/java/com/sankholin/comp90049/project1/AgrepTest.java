package com.sankholin.comp90049.project1;

import com.sankholin.comp90049.project1.tool.Utilities;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AgrepTest {

    private File configFile = new File("./config.properties");
    private Configuration config;
    private File tweetsFile;
    private File gazetteerFile;
    private Utilities util = Utilities.getInstance();
    private StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
    private SimpleAnalyzer simpleAnalyzer = new SimpleAnalyzer();

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
            gazetteerFile = new File(config.getString("gazetteer"));
            gazetteer = FileUtils.readLines(gazetteerFile, "UTF-8");

            logger.info("Tweet collection: " +config.getString("tweets"));
            logger.info("Gazetteer: " +config.getString("gazetteer"));

            logger.info("Agrep path: " +config.getString("cmd.agrep"));

            agrepPath = config.getString("cmd.agrep");

        } catch (ConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test @Ignore
    public void testAgrep() {
        try {

            String str = "@trapped it's really good!!! It is made by a brewery in San Fransisco... It has a hint of watermelon but it is not over powering!!";
            List<String> stringList = util.tokenizeString(standardAnalyzer, str);
            String s = String.join(" ", stringList);

            List<String> commands = new ArrayList<>();
            commands.add(agrepPath);
            commands.add("-1");
            //commands.add("-c");
            commands.add("San Francisco");
            commands.add(tweetsFile.getPath());
            //commands.add(s);
            //commands.add(gazetteerFile.getPath());

            ProcessBuilder builder = new ProcessBuilder();
            builder.redirectErrorStream(true);
            builder.command(commands);

            Process p = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            System.out.printf("\nRunning %s:\n\n", Arrays.toString(commands.toArray()));
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitValue = p.waitFor();
            System.out.println("\n\nExit with: " + exitValue);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static final Logger logger = LogManager.getLogger(AgrepTest.class);
}

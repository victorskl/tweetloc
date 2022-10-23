[![DOI](https://zenodo.org/badge/74888344.svg)](https://zenodo.org/badge/latestdoi/74888344)

About
-----
The **tweetloc** application attempt to perform [Approximate String Searching](https://www.google.com.au/search?q=Approximate+String+Search) and, experiment some String Matching algorithms (<a href="#algo">see below</a>) and observe their effectiveness. It is using a gazetteer dictionary to approximate string match of a possible location name from Twitter user tweets. 

Ideally, String Matching algorithms always perform an exact or best matching on the two comparing strings. However, the **tweetloc** main goal is trying to find possible _**Misspelled Location Names in Tweets**_.


Input Data Assumptions
----------------------
In order to run and experiment **tweetloc** application, you should have the two input data in the following format.

Gazetteer Dictionary

1. Download [Free Gazetteer data from GeoNames](http://download.geonames.org/export/dump/) e.g. `US.zip`
2. Process it to extract the column `asciiname` field data only. e.g. `US.txt`
3. Optionally, sort the location names and remove duplicates.
4. Save this gazetteer data in the plain text `.txt` format and specify it in [`config.properties`](config.properties), `gazetteer=path/to/file.txt`

Tweet Data

1. Harvest Twitter user public tweets data e.g. using [twitter4j](http://twitter4j.org/en/) with [Twitter API](https://dev.twitter.com/docs)
2. And process it in the following format:
        
        user_id (tab) tweet_id (tab) tweet_text (tab) times_tamp (newline)
   
3. Save it as plain text `.txt` format and specify it in [`config.properties`](config.properties), `tweets=path/to/file.txt`

Building Source
-------------------
At core, **tweetloc** make use of the following key libraries:

* [Apache Lucene Core](http://lucene.apache.org/core/)
* [Apache OpenNLP](https://opennlp.apache.org/)
* [Apache Common](http://commons.apache.org/)
* [Agrep](https://www.tgries.de/agrep/)

Please refer [`tweetloc/pom.xml`](pom.xml) for details.

**tweetloc** can build with maven.

    cd tweetloc
    mvn clean
    mvn test
    mvn package

The build artifacts can find under `tweetloc/target` folder.


Configuration
-------------
1.  Open `config.properties` and configure all the paths.
2.  Adjust other parameters. Default values are a good starting point.


Running First Time
------------------
If config is not under the same root as where `tweetloc.jar` is, then pass `-c` option. e.g.

    java -jar tweetloc.jar -c /path/to/config.properties  [... and other options]

Preprocess Gazetteer (Run once)

    java -jar tweetloc.jar -d 1 --preprocess gaze

Partition Tweets (Optional, if Tweets corpus is very big)

    java -jar tweetloc.jar --preprocess parted


Dry Run
-------
To dry run the first few tweets with GED

    java -jar tweetloc.jar -d 3


<a name="algo"></a>Algorithms
----------
You can pass `-a` option to specify the algorithm.

    java -jar tweetloc.jar -a led -d 2

The following are the implemented String Matching algorithms. More algorithms can be developed by implementing [`StringSearch.java`](src/main/java/com/sankholin/comp90049/project1/StringSearch.java) interface.

* ged = Global Edit Distance (default if no -a is pass)
* led = Local Edit Distance
* ngm = N-Gram Distance
* sdx = Soundex
* nbh = Neighbourhood Search (Agrep wrapper)


Specify Output File
-------------------
    java -jar tweetloc.jar -a ngm -o output_ngm.csv -d 5


Start index at 15 and run 5 more lines
--------------------------------------
    java -jar tweetloc.jar -a sdx -o output_sdx.csv -i 15 -d 5


Upper/lower limit (low-pass/high-pass filter)
---------------------
e.g. Run GED with cost `[m=1, i,r,d=-1]` with max score not more than 30

    java -jar tweetloc.jar -zz 30

e.g. Run GED with cost `[m=0, i,r,d=1]` (Levenshtein distance) with min score not lower than 2

    java -jar tweetloc.jar -xx 2


Single word matching
-------------------
Run GED with single word matching (i.e. tokens) against dictionary. e.g. San Francisco becomes 'San' and 'Francisco'. Default is multi-word aware matching by using _chunking heuristic approach_.

    java -jar tweetloc.jar --single


Running As a Job
----------------
It is good idea to run with [`screen`](https://www.google.com.au/search?q=linux%20screen) on Linux as a background job.

    screen
    java -jar tweetloc.jar -a ged -o output_ged_00.csv &
    [ctrl + a, d]
    tail -f app.log
    [ctrl + c]
    screen -r
    [ctrl + a, d]


Notes
-----
This assignment work is done for COMP90049 Project 1 assessment 2016 SM2, The University of Melbourne. You can read [the report](report/SanKhoLin_829463_COMP90049_Project1_Report.pdf) on background context, though it discusses more on the data that I have worked with. You may also want to read the related [`tweetlocml`](https://github.com/victorskl/tweetlocml) assignment. The implementation still has room for improvement. You may cite this work as follow.

LaTeX/BibTeX:

    @misc{sanl1,
        author    = {Lin, San Kho},
        title     = {tweetloc - Finding Misspelled Location Names in Tweets},
        year      = {2016},
        url       = {https://github.com/victorskl/tweetloc},
        urldate   = {yyyy-mm-dd}
    }

Further Reading:

* [Detecting Geographical References in the Form of Place Names and Associated Spatial Natural Language](https://www.umiacs.umd.edu/~codepoet/pubs/recognition-special.pdf)
* [Toponym Resolution in Text](https://www.era.lib.ed.ac.uk/handle/1842/1849)

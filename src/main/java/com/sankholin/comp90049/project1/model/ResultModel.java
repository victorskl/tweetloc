package com.sankholin.comp90049.project1.model;

public class ResultModel {

    private String tweetUserId;
    private String tweetId;
    private String tweetText;
    private String tweetTimestamp;
    private String gazetteer;
    private String tweetToken;
    private int score;

    public ResultModel() {}

    public String getTweetUserId() {
        return tweetUserId;
    }

    public void setTweetUserId(String tweetUserId) {
        this.tweetUserId = tweetUserId;
    }

    public String getTweetId() {
        return tweetId;
    }

    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
    }

    public String getTweetText() {
        return tweetText;
    }

    public void setTweetText(String tweetText) {
        this.tweetText = tweetText;
    }

    public String getTweetTimestamp() {
        return tweetTimestamp;
    }

    public void setTweetTimestamp(String tweetTimestamp) {
        this.tweetTimestamp = tweetTimestamp;
    }

    public String getGazetteer() {
        return gazetteer;
    }

    public void setGazetteer(String gazetteer) {
        this.gazetteer = gazetteer;
    }

    public String getTweetToken() {
        return tweetToken;
    }

    public void setTweetToken(String tweetToken) {
        this.tweetToken = tweetToken;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}

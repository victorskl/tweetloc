package com.sankholin.comp90049.project1.model;

public class MatchTermCandidate implements Comparable<MatchTermCandidate> {
    private String gazetteer;
    private String term;
    private int score;

    public MatchTermCandidate() {
    }

    public String getGazetteer() {
        return gazetteer;
    }

    public void setGazetteer(String gazetteer) {
        this.gazetteer = gazetteer;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(MatchTermCandidate o) {
        return this.score - o.getScore();
    }
}

package com.sankholin.comp90049.project1;

import com.sankholin.comp90049.project1.model.MatchTermCandidate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MatchTermCandidateTest {

    private List<MatchTermCandidate> candidateList = new ArrayList<>();

    @Before
    public void before() {
        MatchTermCandidate candidate = new MatchTermCandidate();
        candidate.setTerm("Nwe Yark");
        candidate.setGazetteer("New York");
        candidate.setScore(4);
        candidateList.add(candidate);

        candidate = new MatchTermCandidate();
        candidate.setTerm("New Yokr");
        candidate.setGazetteer("New York");
        candidate.setScore(1);
        candidateList.add(candidate);

        candidate = new MatchTermCandidate();
        candidate.setTerm("Naw Yokr");
        candidate.setGazetteer("New York");
        candidate.setScore(2);
        candidateList.add(candidate);

        candidate = new MatchTermCandidate();
        candidate.setTerm("New York");
        candidate.setGazetteer("New York");
        candidate.setScore(0);
        candidateList.add(candidate);
    }

    @Test
    public void testSorting() {

        Collections.sort(candidateList);

        StringBuilder sb = new StringBuilder();
        for (int i=0; i<3; i++) {
            sb.append(candidateList.get(i).getScore());
        }

        Collections.reverse(candidateList);

        for (int i=0; i<3; i++) {
            sb.append(candidateList.get(i).getScore());
        }

        assertEquals("012421", sb.toString());
    }

    @Test
    public void testSortingWithStream() {

        StringBuilder sb = new StringBuilder();
        for (Object o : candidateList.stream().sorted().limit(3).toArray()) {
            MatchTermCandidate c = (MatchTermCandidate) o;
            //System.out.println(c.getTerm() + " \t" + c.getGazetteer() + " \t" + c.getScore());
            sb.append(c.getScore());
        }

        for (Object o : candidateList.stream().sorted(Comparator.reverseOrder()).limit(3).toArray()) {
            MatchTermCandidate c = (MatchTermCandidate) o;
            //System.out.println(c.getTerm() + " \t" + c.getGazetteer() + " \t" + c.getScore());
            sb.append(c.getScore());
        }

        assertEquals("012421", sb.toString());
    }
}

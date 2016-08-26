package com.sankholin.comp90049.project1.editdistance;

import com.sankholin.comp90049.project1.StringSearch;

interface EditDistance extends StringSearch {
    boolean isMinimalScore();
    void printScore();
}

package com.homeproject.controlbot.comparator;

import com.homeproject.controlbot.entity.Earning;

import java.sql.Timestamp;
import java.util.Comparator;

public class EarningDateComparator implements Comparator<Earning> {

    @Override
    public int compare(Earning o1, Earning o2) {
        Timestamp firstDate = o1.getEarnedAt();
        Timestamp secondDate = o2.getEarnedAt();
        return firstDate.compareTo(secondDate);
    }
}

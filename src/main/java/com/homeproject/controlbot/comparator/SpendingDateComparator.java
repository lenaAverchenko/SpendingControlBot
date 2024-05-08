package com.homeproject.controlbot.comparator;

import com.homeproject.controlbot.entity.Spending;


import java.sql.Timestamp;
import java.util.Comparator;

public class SpendingDateComparator implements Comparator<Spending> {
    @Override
    public int compare(Spending o1, Spending o2) {
        Timestamp firstDate = o1.getSpentAt();
        Timestamp secondDate = o2.getSpentAt();
        return firstDate.compareTo(secondDate);
    }
}

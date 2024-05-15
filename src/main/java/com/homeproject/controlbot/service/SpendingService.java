package com.homeproject.controlbot.service;

import com.homeproject.controlbot.entity.Spending;

import java.util.List;

public interface SpendingService {
    String deleteSpending(long id, long chatIdCheck);
    List<Spending> findAllSpending(long chatId);
    List<Spending> findAllSpendingOfTheYear(int year, long chatId);
    List<Spending> findAllSpendingOfTheCurrentYear(long chatId);
    List<Spending> findAllSpendingOfTheMonth(int monthNumber, int year, long chatId);
    List<Spending> findAllSpendingOfTheCurrentMonth(long chatId);
    List<Spending> findAllSpendingOfTheCurrentDay(long chatId);
    List<Spending> findAllSpendingOfTheDay(int spentDay, int spentMonth, int spentYear, long chatId);
}

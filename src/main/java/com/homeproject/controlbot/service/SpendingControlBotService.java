package com.homeproject.controlbot.service;

import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.entity.Earning;
import com.homeproject.controlbot.entity.Spending;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import java.util.List;

public interface SpendingControlBotService {

    BotUser getBotUserInformation();
    String deleteBotUserInformation();
    void setSpending();
    void setEarning();
    void setSpendingOfTheDay(int day, int month, int year);
    void setEarningOfTheDay(int day, int month, int year);

    List<Spending> findAllSpending();
    List<Spending> findAllSpendingOfTheYear(int year);
    List<Spending> findAllSpendingOfTheCurrentYear();
    List<Spending> findAllSpendingOfTheMonth(int monthNumber, int year);
    List<Spending> findAllSpendingOfTheCurrentMonth();
    List<Spending> findAllSpendingOfTheCurrentDay();
    List<Spending> findAllSpendingOfTheDay(int day, int month, int year);
    List<Earning> findAllEarning();
    List<Earning> findAllEarningOfTheYear(int year);
    List<Earning> findAllEarningOfTheCurrentYear();
    List<Earning> findAllEarningOfTheMonth(int monthNumber, int year);
    List<Earning> findAllEarningOfTheCurrentMonth();
    void deleteSpending(long id);
    void deleteEarning(long id);



}

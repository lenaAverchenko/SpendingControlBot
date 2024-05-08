package com.homeproject.controlbot.service;

import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.entity.Earning;
import com.homeproject.controlbot.entity.Spending;
import com.homeproject.controlbot.enums.TypeOfEarning;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public interface SpendingControlBotService {

    BotUser getBotUserInformation(Message message);
    String deleteBotUserInformation(Update update);
    public void setSpending(long chatId, BigDecimal spentSum);
    public void setSpendingByDate(long chatId, BigDecimal spentSum, Timestamp date);
    void setEarning(long chatId, BigDecimal sum);
    void setEarningByDate(long chatId, BigDecimal sum, Timestamp date);

    List<Spending> findAllSpending();
    List<Spending> findAllSpendingOfTheYear(int year);
    List<Spending> findAllSpendingOfTheCurrentYear();
    List<Spending> findAllSpendingOfTheMonth(int monthNumber, int year);
    List<Spending> findAllSpendingOfTheCurrentMonth();
    List<Spending> findAllSpendingOfTheCurrentDay();
    List<Spending> findAllSpendingOfTheDay(int day, int month, int year);
    List<Earning> findAllEarning(long chatId);
    List<Earning> findAllEarningOfTheYear(int year, long chatId);
    List<Earning> findAllEarningOfTheCurrentYear(long chatId);
    List<Earning> findAllEarningOfTheMonth(int monthNumber, int year, long chatId);
    List<Earning> findAllEarningOfTheCurrentMonth(long chatId);
    void deleteSpending(long id);
    void deleteEarning(long id, long chatId);



}

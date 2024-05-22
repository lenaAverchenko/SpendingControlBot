package com.homeproject.controlbot.service;

import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.entity.Earning;
import com.homeproject.controlbot.enums.TypeOfEarning;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public interface EarningService {
    String saveEarning(long chatId, Timestamp date, BotUser botUser, TypeOfEarning typeOfEarning, BigDecimal earnedSum);
    String saveEarningByDate(long chatId, Timestamp date, BotUser botUser, TypeOfEarning typeOfEarning, BigDecimal earnedSum);
    String deleteEarning(long id, long chatIdCheck);
    List<Earning> findAllEarning(long chatId);
    List<Earning> findAllEarningOfTheYear(int year, long chatId);
    List<Earning> findAllEarningOfTheCurrentYear(long chatId);
    List<Earning> findAllEarningOfTheMonth(int monthNumber, int year, long chatId);
    List<Earning> findAllEarningOfTheDay(int day, int monthNumber, int year, long chatId);
    List<Earning> findAllEarningOfTheCurrentMonth(long chatId);
}

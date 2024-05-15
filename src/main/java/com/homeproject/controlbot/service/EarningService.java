package com.homeproject.controlbot.service;

import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.enums.TypeOfEarning;

import java.math.BigDecimal;
import java.sql.Timestamp;

public interface EarningService {
    boolean saveEarning(long chatId, Timestamp date, BotUser botUser, TypeOfEarning typeOfEarning, BigDecimal earnedSum);
    boolean saveEarningByDate(long chatId, Timestamp date, BotUser botUser, TypeOfEarning typeOfEarning, BigDecimal earnedSum);

}

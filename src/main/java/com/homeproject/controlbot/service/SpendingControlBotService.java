package com.homeproject.controlbot.service;

import com.homeproject.controlbot.entity.BotUser;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;


import java.math.BigDecimal;
import java.sql.Timestamp;

public interface SpendingControlBotService {

    BotUser getBotUserInformation(Message message);

    String deleteBotUserInformation(Update update);

    void setSpending(long chatId, BigDecimal spentSum);

    void setSpendingByDate(long chatId, BigDecimal spentSum, Timestamp date);

    void setEarning(long chatId, BigDecimal sum);

    void setEarningByDate(long chatId, BigDecimal sum, Timestamp date);
}

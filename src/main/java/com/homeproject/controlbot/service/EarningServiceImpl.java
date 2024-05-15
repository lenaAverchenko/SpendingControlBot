package com.homeproject.controlbot.service;

import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.entity.Earning;
import com.homeproject.controlbot.enums.Marker;
import com.homeproject.controlbot.enums.TypeOfEarning;
import com.homeproject.controlbot.repository.EarningRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class EarningServiceImpl implements EarningService{

    @Autowired
    private EarningRepository earningRepository;

    @Override
    public boolean saveEarning(long chatId, Timestamp date, BotUser botUser, TypeOfEarning typeOfEarning, BigDecimal earnedSum) {
        log.info("setEarning has been started");
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        return saveEarningByDate(chatId, date, botUser, typeOfEarning, earnedSum);
    }

    @Override
    public boolean saveEarningByDate(long chatId, Timestamp date, BotUser botUser, TypeOfEarning typeOfEarning, BigDecimal earnedSum) {
        log.info("setEarningByDate has been started");
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Earning earning = new Earning();
        earning.setRegisteredAt(currentTime);
        earning.setBotUser(botUser);
        earning.setEarnedAt(date);
        earning.setTypeOfEarning(typeOfEarning);
        earning.setEarningSum(earnedSum);
        earningRepository.save(earning);
        if (earningRepository.findById(earning.getEarningId()).isPresent()) {
            return true;
        }
        return false;
    }

}

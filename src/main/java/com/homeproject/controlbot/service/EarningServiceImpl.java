package com.homeproject.controlbot.service;

import com.homeproject.controlbot.comparator.EarningDateComparator;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class EarningServiceImpl implements EarningService{

    @Autowired
    private EarningRepository earningRepository;

    @Override
    public String saveEarning(long chatId, Timestamp date, BotUser botUser, TypeOfEarning typeOfEarning, BigDecimal earnedSum) {
        log.info("setEarning has been started");
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        return saveEarningByDate(chatId, date, botUser, typeOfEarning, earnedSum);
    }

    @Override
    public String saveEarningByDate(long chatId, Timestamp date, BotUser botUser, TypeOfEarning typeOfEarning, BigDecimal earnedSum) {
        log.info("setEarningByDate has been started");
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Earning earning = new Earning();
        earning.setRegisteredAt(currentTime);
        earning.setBotUser(botUser);
        earning.setEarnedAt(date);
        earning.setTypeOfEarning(typeOfEarning);
        earning.setEarningSum(earnedSum);
        earningRepository.save(earning);
//        boolean resultIsTrue = earningService.saveEarningByDate(chatId, date, botUserRepository.findById(chatId).orElse(null),
//                typeOfEarning, earnedSum);
        if (earningRepository.findById(earning.getEarningId()).isPresent()) {
            return "The provided data about your recent earning has been successfully saved.";
        }
        return "The provided data about your recent earning hasn't been saved. Please, try again.";
    }

    @Override
    public String deleteEarning(long id, long chatIdCheck) {
        log.info("deleteEarning method was called by " + chatIdCheck + ". Deleting of the earning by id: " + id);
        Earning earningToDelete = earningRepository.findById(id).orElse(null);
        long chatId = 0;
        if (earningToDelete != null) {
            chatId = earningToDelete.getBotUser().getId();
        }
        if (chatId == chatIdCheck) {
            earningRepository.deleteById(id);
            if (earningRepository.findById(id).orElse(null) == null) {
                return "Earning data with the id: " + id + " has been successfully deleted";
            }
        } else {
            return "You are trying to delete not your earning. You can check " +
                    "saved data for your account and try to delete one of them again.";
        }
        return "Couldn't delete the earning";
    }

    @Override
    public List<Earning> findAllEarning(long chatId) {
        log.info("findAllEarning method was called by " + chatId);
        return  earningRepository.findAll().stream()
                .filter(earn -> earn.getBotUser().getId() == chatId)
                .sorted(new EarningDateComparator()).toList();
    }

    @Override
    public List<Earning> findAllEarningOfTheYear(int year, long chatId) {
        log.info("findAllEarningOfTheYear method was called by " + chatId);
        return findAllEarning(chatId).stream()
                .filter(x -> (int) x.getEarnedAt().toLocalDateTime().getYear() == year)
                .toList();
    }

    @Override
    public List<Earning> findAllEarningOfTheCurrentYear(long chatId) {
        log.info("findAllEarningOfTheCurrentYear method was called by " + chatId);
        return findAllEarningOfTheYear(LocalDate.now().getYear(), chatId);
    }

    @Override
    public List<Earning> findAllEarningOfTheMonth(int monthNumber, int year, long chatId) {
//        log.info("findAllEarningOfTheMonth method was called by " + chatId);
        return findAllEarning(chatId).stream()
                .filter(x -> ((int) x.getEarnedAt().toLocalDateTime().getYear() == year))
                .filter(y -> (y.getEarnedAt().toLocalDateTime().getMonth().getValue() == monthNumber))
                .toList();
    }

    public List<Earning> findAllEarningOfTheDay(int day, int monthNumber, int year, long chatId) {
        log.info("findAllEarningOfTheDay method was called by " + chatId);
        return findAllEarning(chatId).stream()
                .filter(x -> ((int) x.getEarnedAt().toLocalDateTime().getYear() == year))
                .filter(y -> (y.getEarnedAt().toLocalDateTime().getMonth().getValue() == monthNumber))
                .filter(y -> (y.getEarnedAt().toLocalDateTime().getDayOfMonth() == day))
                .toList();
    }

    @Override
    public List<Earning> findAllEarningOfTheCurrentMonth(long chatId) {
        log.info("findAllEarningOfTheCurrentMonth method was called by " + chatId);
        return findAllEarningOfTheMonth(LocalDate.now().getMonth().getValue(), LocalDate.now().getYear(), chatId);
    }

}

package com.homeproject.controlbot.service;

import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.entity.Earning;
import com.homeproject.controlbot.entity.Spending;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpendingControlBotServiceImpl implements SpendingControlBotService{
    @Override
    public BotUser getBotUserInformation() {
        return null;
    }

    @Override
    public String deleteBotUserInformation() {
        return null;
    }

    @Override
    public void setSpending() {

    }

    @Override
    public void setEarning() {

    }

    @Override
    public List<Spending> findAllSpending() {
        return null;
    }

    @Override
    public List<Spending> findAllSpendingOfTheYear(int year) {
        return null;
    }

    @Override
    public List<Spending> findAllSpendingOfTheCurrentYear() {
        return null;
    }

    @Override
    public List<Spending> findAllSpendingOfTheMonth(int monthNumber, int year) {
        return null;
    }

    @Override
    public List<Spending> findAllSpendingOfTheCurrentMonth() {
        return null;
    }

    @Override
    public List<Spending> findAllSpendingOfTheCurrentDay() {
        return null;
    }

    @Override
    public List<Spending> findAllSpendingOfTheDay(int day, int month, int year) {
        return null;
    }

    @Override
    public List<Earning> findAllEarning() {
        return null;
    }

    @Override
    public List<Earning> findAllEarningOfTheYear(int year) {
        return null;
    }

    @Override
    public List<Earning> findAllEarningOfTheCurrentYear() {
        return null;
    }

    @Override
    public List<Earning> findAllEarningOfTheMonth(int monthNumber, int year) {
        return null;
    }

    @Override
    public List<Earning> findAllEarningOfTheCurrentMonth() {
        return null;
    }
}

package com.homeproject.controlbot.service;

import com.homeproject.controlbot.configuration.BotConfig;
import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.entity.Earning;
import com.homeproject.controlbot.entity.Spending;
import com.homeproject.controlbot.repository.BotUserRepository;
import com.homeproject.controlbot.repository.EarningRepository;
import com.homeproject.controlbot.repository.SpendingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SpendingControlBotServiceImpl extends TelegramLongPollingBot implements SpendingControlBotService {

    @Autowired
    private BotUserRepository botUserRepository;

    @Autowired
    private EarningRepository earningRepository;

    @Autowired
    private SpendingRepository spendingRepository;

    final private BotConfig botConfig;
    final String ERROR_TEXT = "The error occurred: ";
    static final String HELP_TEXT = "This bot is created to calculate, to check and to verify your spendings and earnings \n\n" +
            "You can execute commands from the main menu on the left or by typing a command. \n\n" +
            "Type /start to begin using Spending Control Bot \n\n" +
            "Type /setearning to input information about your earnings\n\n" +
            "Type /settodayspending to input information about today's spending\n\n" +
            "Type /setspending to input information about another spending you made and want to store\n\n" +
            "Type /findspending to see the information about your previous spending\n\n" +
            "Type /findearning to see the information about your previous earning\n\n" +
            "Type /deletespending to delete the wrong spending from database\n\n" +
            "Type /deleteearning to delete the wrong earning from database\n\n" +
            "Type /data to get info about the stored data from your account\n\n" +
            "Type /deletedata to delete stored info about your account\n\n" +
            "Type /help to see this message again.";


    private SpendingControlBotServiceImpl (BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "start working with spendingControlBot"));
        listOfCommands.add(new BotCommand("/setearning", "set information about earning"));
        listOfCommands.add(new BotCommand("/settodayspending", "set information about Today's spending"));
        listOfCommands.add(new BotCommand("/setspending", "set information about some day's spending"));
        listOfCommands.add(new BotCommand("/findspending", "find spending according to your preferences"));
        listOfCommands.add(new BotCommand("/findearning", "find earning according to your preferences"));
        listOfCommands.add(new BotCommand("/deletespending", "delete previously added spending"));
        listOfCommands.add(new BotCommand("/deleteearning", "delete previously added earning"));
        listOfCommands.add(new BotCommand("/data", "get stored information about my account"));
        listOfCommands.add(new BotCommand("/deletedata", "delete stored information about my account"));
        listOfCommands.add(new BotCommand("/help", "get information about how the bot works"));
        try{
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

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
    public void setSpendingOfTheDay(int day, int month, int year) {

    }

    @Override
    public void setEarningOfTheDay(int day, int month, int year) {

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

//    @Override
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

    @Override
    public void deleteSpending(long id) {

    }

    @Override
    public void deleteEarning(long id) {

    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String receivedMessage = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String firstNameOfTheUser = update.getMessage().getChat().getFirstName();
            switch (receivedMessage){
                case "/start":
                    log.info("Command /start was called by " + firstNameOfTheUser);
                    startCommandReceived(chatId, firstNameOfTheUser);
                    break;
                case "/setEarning":
                    break;
                case "/setTodaySpending":
                    break;
                case "/setSpending":
                    break;
                case "/findSpending":
                    break;
                case "/findEarning":
                    break;
                case "/deletespending":
                    break;
                case "/deleteearning":
                    break;
                case "/data":
                    break;
                case "/deleteData":
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId, "Sorry, the command was not recognized");
                    break;

            }
        }

    }

    private void startCommandReceived(long chatId, String name){
        String answer = "Hi, " + name + "! Let's start working! What are we going to do right now?";
        sendMessage(chatId, answer);
        log.info("Replied to user " + name);
    }
    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try{
            execute(message);
        } catch (TelegramApiException e) {
//            TODO
            log.error(ERROR_TEXT + e.getMessage());
        }
    }
}

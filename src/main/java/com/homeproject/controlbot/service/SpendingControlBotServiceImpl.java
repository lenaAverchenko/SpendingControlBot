package com.homeproject.controlbot.service;

import com.homeproject.controlbot.configuration.BotConfig;
import com.homeproject.controlbot.entity.AutomatedMessage;
import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.entity.Earning;
import com.homeproject.controlbot.entity.Spending;
import com.homeproject.controlbot.repository.AutomatedMessageRepository;
import com.homeproject.controlbot.repository.BotUserRepository;
import com.homeproject.controlbot.repository.EarningRepository;
import com.homeproject.controlbot.repository.SpendingRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
public class SpendingControlBotServiceImpl extends TelegramLongPollingBot implements SpendingControlBotService {

    @Autowired
    private BotUserRepository botUserRepository;

    @Autowired
    private EarningRepository earningRepository;

    @Autowired
    private SpendingRepository spendingRepository;
    @Autowired
    private AutomatedMessageRepository automatedMessageRepository;

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
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            Message receivedMessage = update.getMessage();
            String receivedMessageText = receivedMessage.getText();
            long chatId = receivedMessage.getChatId();
            String firstNameOfTheUser = receivedMessage.getChat().getFirstName();
            if (receivedMessageText.contains("/send") && (botConfig.getBotOwnerId() == chatId)) {
                log.info("send command was called");
                String textToSend = EmojiParser.parseToUnicode(receivedMessageText.substring(
                        receivedMessageText.indexOf(" ")));
                List<BotUser> users = botUserRepository.findAll();
                for (BotUser user : users) {
                    sendMessage(user.getId(), textToSend);
                }
            } else {
                switch (receivedMessageText) {
                    case "/start":
                        log.info("Command /start was called by " + firstNameOfTheUser);
                        startCommandReceived(chatId, firstNameOfTheUser);
                        registerBotUser(update);
                        break;
                    case "/setearning":
                        break;
                    case "/settodayspending":
                        break;
                    case "/setspending":
                        break;
                    case "/findSpending":
                        break;
                    case "/findearning":
                        break;
                    case "/deletespending":
                        break;
                    case "/deleteearning":
                        break;
                    case "/data":
                        break;
                    case "/deletedata":
                        deleteBotUserInformation(update);
                        break;
                    case "/help":
                        sendMessage(chatId, HELP_TEXT);
                        break;
                    case "/register":
                        registerBotUser(update);
                        break;
                    default:
                        sendMessage(chatId, "Sorry, the command was not recognized");
                        break;

                }
        }

        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            Message callbackMessage = update.getCallbackQuery().getMessage();
            long messageId = callbackMessage.getMessageId();
            long chatId = callbackMessage.getChatId();
            String answerText = null;
            switch (callBackData){
                case "YES_BUTTON":
                    answerText = registrationPermittedAct(callbackMessage);
                    break;
                case "NO_BUTTON":
                    answerText = "You pressed NO button. Access to the Bot is not allowed. Try again";
                    break;
                case "YES_DELETE_BUTTON":
                    log.info("yes delete button pressed");
                    botUserRepository.delete(Objects.requireNonNull(botUserRepository.findById(chatId)).orElse(null));
                    answerText = "Data for your account has been deleted.";
                    log.info("information about the user is deleted");
                    break;
                case "NO_DELETE_BUTTON":
                    log.info("no button pressed");
                    answerText = "I won't delete the data for your account. You can press start to continue.";
                    break;
                default:
                    break;
            }
            sendEditedMessage(chatId, (int) messageId, answerText);
        }

    }

    private void registerBotUser(Update update){
        Message message = update.getMessage();
        String userId = message.getChat().getUserName();
        log.info("Registration method was called by " + userId);
        if (botUserRepository.findById(message.getChatId()).isEmpty()){
            log.info("there is no such user. He's needed to be saved");
            sendMessageWithButtons(message.getChatId(), "But first of all. Do you truly want to register and continue?", List.of(List.of("YES", "NO")));
//            TODO - what's happening when we are pressing yes or no
            log.info("registration question was asked");
        }
    }

    private String registrationPermittedAct(Message message){
        log.info("registrationPermittedAct method was called ");
        String notificationMessage = "You pressed YES button. I'm saving your data.";
        long chatId = message.getChatId();
        Chat chat = message.getChat();
        BotUser user = new BotUser();
        user.setId(chatId);
        user.setUserName(message.getChat().getUserName());
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setSpendingList(new ArrayList<>());
        user.setEarningList(new ArrayList<>());
        user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
        botUserRepository.save(user);
        log.info("User saved with the information: " + user);
        return notificationMessage;
    }


    @Override
    public BotUser getBotUserInformation() {
        return null;
    }

    @Override
    public String deleteBotUserInformation(Update update) {
        log.info("deleteBotUserInformation was called");
        sendMessageWithButtons(update.getMessage().getChatId(),"Do you really want to delete all your data from database?", List.of(List.of("YES_DELETE", "NO_DELETE")));
        Message receivedMessage = update.getMessage();
        String notificationMessage = null;
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


    private boolean expectingButtonPressed (String callBackData, String expectingNameOfButton){
//            Получаем идентификатор кнопки
        log.info("yesButtonPressed was called");
        if (callBackData.equals(expectingNameOfButton)){
            return true;
        }
        return false;
    }

    private void startCommandReceived(long chatId, String name){
        String answer = EmojiParser.parseToUnicode("Hi, " + name + "!" + ":blush:" + " Let's start working!" + ":computer:" + " What are we going to do right now?" + ":arrow_down:");
        List<String> buttonNames = List.of("/start", "/setearning", "/settodayspending", "/setspending",
                "/findspending", "/findearning", "/deletespending", "/deleteearning", "/data", "/deletedata", "/help");
        sendMessageWithKeyboard(chatId, answer, buttonNames);
        log.info("Replied to user " + name);
    }
    private void sendMessageWithKeyboard(long chatId, String textToSend, List<String> buttonNames){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        if(!buttonNames.isEmpty()){
            createKeyboardForRequest(buttonNames, sendMessage);
        }
        executeMessage(sendMessage);
    }
    private void sendMessageWithButtons(long chatId, String textToSend, List<List<String>> buttonNames){
        log.info("sendMessageWithButtons was called");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        if(!buttonNames.isEmpty()){
            createButtonsInMessage(sendMessage, buttonNames);
        }
        executeMessage(sendMessage);
    }

    private void createButtonsInMessage(SendMessage sendMessage, List<List<String>> buttonNames){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> lists = new ArrayList<>();
        for (List<String> list:buttonNames) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (String buttonName:list){
                InlineKeyboardButton inlineKeyboardButton =new InlineKeyboardButton();
                inlineKeyboardButton.setText(buttonName);
                inlineKeyboardButton.setCallbackData(buttonName + "_BUTTON");
                buttons.add(inlineKeyboardButton);
            }
            lists.add(buttons);
        }
        markup.setKeyboard(lists);
        sendMessage.setReplyMarkup(markup);
    }

    private void sendMessage(long chatId, String textToSend){
        log.info("sendMessage was called");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        executeMessage(sendMessage);
    }
    private void createKeyboardForRequest(List<String> buttonNames, SendMessage sendMessage){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        int counter = 0;
        for (String button: buttonNames) {
            if (counter == 3){
                keyboardRows.add(row);
                counter = 0;
                row = new KeyboardRow();
            }
            row.add(button);
            counter += 1;
        }
        if (!row.isEmpty()){
            keyboardRows.add(row);
        }
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
    }

    private void sendEditedMessage(long chatId, int messageId, String notificationMessage){
        EditMessageText messageText = new EditMessageText();
        messageText.setChatId(String.valueOf(chatId));
        messageText.setText(notificationMessage);
        messageText.setMessageId((int) messageId);
        try{
            execute(messageText);
        } catch (TelegramApiException e){
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void executeMessage(SendMessage sendMessage){
        try{
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    @Scheduled(cron = "${cron.scheduler}")
//    @Scheduled(cron = "0 * * * * *") - раз в минуту. Выше - раз в месяц
    private void sendAutoMessage(){
        List<AutomatedMessage> automatedMessageList = automatedMessageRepository.findAll();
        List<BotUser> botUsersList = botUserRepository.findAll();
        for (AutomatedMessage mes:automatedMessageList) {
            for (BotUser user:botUsersList) {
                sendMessage(user.getId(), mes.getAdMessage());
            }
        }
    }
}

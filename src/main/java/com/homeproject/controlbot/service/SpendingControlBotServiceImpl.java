package com.homeproject.controlbot.service;

import com.homeproject.controlbot.comparator.EarningDateComparator;
import com.homeproject.controlbot.configuration.BotConfig;
import com.homeproject.controlbot.entity.AutomatedMessage;
import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.entity.Earning;
import com.homeproject.controlbot.entity.Spending;
import com.homeproject.controlbot.enums.TypeOfEarning;
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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;


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
    private TypeOfEarning typeOfEarning;

    private BigDecimal earnedSum;
    private String earningDate;
    private int earningIndicator;
    private int deleteEarningMarker;
    private Long currentId;
    final private BotConfig botConfig;
    final String ERROR_TEXT = "The error occurred: ";
    static final String HELP_TEXT = "This bot is created to calculate, to check and to verify your spendings and earnings \n\n" +
            "You can execute commands from the main menu on the left or by typing a command. \n\n" +
            "Type /start to begin using Spending Control Bot \n\n" +
            "Type /setearning to input information about your today's earnings\n\n" +
            "Type /setearningbydate to input information about your earning for particular day \n\n" +
            "Type /settodayspending to input information about today's spending\n\n" +
            "Type /setspending to input information about another spending you made and want to store\n\n" +
            "Type /findspending to see the information about your previous spending\n\n" +
            "Type /findearning to see the information about your previous earning\n\n" +
            "Type /deletespending to delete the wrong spending from database\n\n" +
            "Type /deleteearning to delete the wrong earning from database\n\n" +
            "Type /data to get info about the stored data from your account\n\n" +
            "Type /deletedata to delete stored info about your account\n\n" +
            "Type /help to see this message again.";

    private SpendingControlBotServiceImpl(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "start working with spendingControlBot"));
        listOfCommands.add(new BotCommand("/setearning", "set information about earning"));
        listOfCommands.add(new BotCommand("/setearningbydate", "set information about earning of the particular day"));
        listOfCommands.add(new BotCommand("/settodayspending", "set information about Today's spending"));
        listOfCommands.add(new BotCommand("/setspending", "set information about some day's spending"));
        listOfCommands.add(new BotCommand("/findspending", "find spending according to your preferences"));
        listOfCommands.add(new BotCommand("/findearning", "find earning according to your preferences"));
        listOfCommands.add(new BotCommand("/deletespending", "delete previously added spending"));
        listOfCommands.add(new BotCommand("/deleteearning", "delete previously added earning"));
        listOfCommands.add(new BotCommand("/data", "get stored information about my account"));
        listOfCommands.add(new BotCommand("/deletedata", "delete stored information about my account"));
        listOfCommands.add(new BotCommand("/help", "get information about how the bot works"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message receivedMessage = update.getMessage();
            String receivedMessageText = receivedMessage.getText();
            long chatId = receivedMessage.getChatId();
            String firstNameOfTheUser = receivedMessage.getChat().getFirstName();
            if (update.getMessage().getText().startsWith("/")) {
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
                            setEarningProcess(chatId);
                            break;
                        case "/setearningbydate":
                            setEarningByDateProcess(chatId);
                            break;
                        case "/settodayspending":
                            break;
                        case "/setspending":
                            break;
                        case "/findSpending":
                            break;
                        case "/findearning":
                            List<List<String>> allTheButtons = List.of(List.of("All earnings"),
                                    List.of("Earnings of this year", "Earnings of the year ..."),
                                    List.of("Earnings of this month", "Earnings of the month ..."),
                                    List.of("Earnings of the day"));
                            sendMessageWithButtons(chatId, "Pick the search you want to proceed with: ", allTheButtons);
                            break;
                        case "/deletespending":
                            break;
                        case "/deleteearning":
                            deleteEarningMarker = 1;
                            sendMessage(chatId, "What is the ID of the earning you want to delete?");
                            break;
                        case "/data":
                            sendMessage(chatId, getBotUserInformation(receivedMessage).toString());
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
            } else {
                String idPattern = "\\d+";
                if (deleteEarningMarker == 1 && Pattern.matches(idPattern, receivedMessageText)){
                    currentId = Long.parseLong(receivedMessageText);
                    deleteEarning(currentId);
                } else if(deleteEarningMarker == 1 && !Pattern.matches(idPattern, receivedMessageText)) {
                    deleteEarningMarker = 0;
                    currentId = null;
                }
                String dataPattern = "\\d{2}-\\d{2}-\\d{4}";
                if (Pattern.matches(dataPattern, receivedMessageText) && earningIndicator == 1){
                    earningDate = receivedMessageText;
                    setEarningProcess(chatId);
                } else if (earningIndicator ==1 && !Pattern.matches(dataPattern, receivedMessageText)) {
                    sendMessage(chatId, "The provided date has wrong format. You can try again.");
                    earningDate = null;
                    earningIndicator = 0;
                }
                if (typeOfEarning != null) {
                    String numberPattern = "^\\d*\\.?\\d+$";
                    if (Pattern.matches(numberPattern, receivedMessageText) && earningIndicator ==1) {
                        log.info("input text is: " + receivedMessageText);
                        earnedSum = new BigDecimal(receivedMessageText);
                        log.info("sum is: " + earnedSum);
                        if (earningDate == null){
                        setEarning(chatId, earnedSum);
                        } else{
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            try {
                                setEarningByDate(chatId, earnedSum, new Timestamp((dateFormat.parse(earningDate).getTime())));
                            } catch (ParseException e) {
                                log.error("An error occurred with parsing string to date: " + e.getMessage());
                            }
                        }
                    } else {
                        earnedSum = null;
                        typeOfEarning = null;
                        log.error("The error occurred with earnedSum - format parsing.");
                        sendMessage(chatId, "Wrong format of the earned sum. You can try again pressing");
                    }
                }

            }

        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            Message callbackMessage = update.getCallbackQuery().getMessage();
            long messageId = callbackMessage.getMessageId();
            long chatId = callbackMessage.getChatId();
            String answerText = null;
            switch (callBackData) {
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
                case "SALARY_BUTTON":
                    log.info("SALARY_BUTTON" + chatId);
                    typeOfEarning = TypeOfEarning.SALARY;
                    setEarningProcess(chatId);
                    break;
                case "CHILD_SUPPORT_BUTTON":
                    log.info("CHILD_SUPPORT_BUTTON" + chatId);
                    typeOfEarning = TypeOfEarning.CHILD_SUPPORT;
                    setEarningProcess(chatId);
                    break;
                case "PRISE_BUTTON":
                    log.info("PRISE_BUTTON" + chatId);
                    typeOfEarning = TypeOfEarning.PRISE;
                    setEarningProcess(chatId);
                    break;
                case "GIFT_BUTTON":
                    log.info("GIFT_BUTTON" + chatId);
                    typeOfEarning = TypeOfEarning.GIFT;
                    setEarningProcess(chatId);
                    break;
                case "All earnings_BUTTON":
                    log.info("All earnings_BUTTON" + chatId);
                    sendMessage(chatId,findAllEarning().toString());
                    break;
                case "Earnings of this year_BUTTON":
                    break;
                case "Earnings of the year ..._BUTTON":
                    break;
                case "Earnings of this month_BUTTON":
                    break;
                case "Earnings of the month ..._BUTTON":
                    break;
                case "Earnings of the day_BUTTON":
                    break;
                default:
                    break;
            }
            sendEditedMessage(chatId, (int) messageId, answerText);
        }

    }

    private void registerBotUser(Update update) {
        Message message = update.getMessage();
        String userId = message.getChat().getUserName();
        log.info("Registration method was called by " + userId);
        if (botUserRepository.findById(message.getChatId()).isEmpty()) {
            log.info("there is no such user. He's needed to be saved");
            sendMessageWithButtons(message.getChatId(), "But first of all. Do you truly want to register and continue?", List.of(List.of("YES", "NO")));
//            TODO - what's happening when we are pressing yes or no
            log.info("registration question was asked");
        }
    }

    private String registrationPermittedAct(Message message) {
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
    public BotUser getBotUserInformation(Message message) {
        return botUserRepository.findById(message.getChatId()).orElse(null);
    }

    @Override
    public String deleteBotUserInformation(Update update) {
        log.info("deleteBotUserInformation was called");
        sendMessageWithButtons(update.getMessage().getChatId(), "Do you really want to delete all your data from database?", List.of(List.of("YES_DELETE", "NO_DELETE")));
        return null;
    }

    @Override
    public void setEarning(long chatId, BigDecimal sum) {
        log.info("setEarning has been started");
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        setEarningByDate(chatId, sum, currentTime);
    }

    public void setEarningProcess(long chatId) {
        log.info("setEarningProcess called with chatId " + chatId);
        earningIndicator = 1;
        if (typeOfEarning == null) {
            log.info("setEarningProcess called and typeOfEarning == null, with chatId " + chatId);
            List<List<String>> earningTypeList = List.of(Arrays.stream(TypeOfEarning.values()).map(v -> v.toString()).toList());
            sendMessageWithButtons(chatId, "Pick the type of earning:", earningTypeList);
        } else {
            log.info("setEarningProcess called and typeOfEarning != null, with chatId " + chatId);
            if (earnedSum == null) {
                log.info("setEarningProcess called and sum ==0 " + chatId);
                sendMessage(chatId, "What is the earned sum of money:");
            }
        }
    }

    @Override
    public void setEarningByDate(long chatId, BigDecimal sum, Timestamp date){
        log.info("setEarningByDate has been started");
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Earning earning = new Earning();
        earning.setRegisteredAt(currentTime);
        earning.setBotUser(botUserRepository.findById(chatId).orElse(null));
        earning.setEarnedAt(date);
        earning.setTypeOfEarning(typeOfEarning);
        earning.setEarningSum(earnedSum);
        earningRepository.save(earning);
        typeOfEarning = null;
        if (!earningRepository.findById(earning.getEarningId()).isEmpty()) {
            sendMessage(chatId, "The provided data about your recent earning has been successfully saved.");
            earnedSum = null;
            earningIndicator = 0;
            earningDate = null;
        }
    }

    public void setEarningByDateProcess(long chatId){
        earningIndicator = 1;
        sendMessage(chatId, "Enter the date of this earning in format (DD-MM-YYYY):");
    }


    @Override
    public void setSpending() {

    }
    @Override
    public void setSpendingOfTheDay(int day, int month, int year) {

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
        List<Earning> earningList = earningRepository.findAll().stream().sorted(new EarningDateComparator()).toList();
        return earningList;
    }

    //    @Override
    public List<Earning> findAllEarningOfTheYear(int year) {
        List<Earning> earningList = earningRepository.findAll().stream().filter(x -> (int)x.getEarnedAt().toLocalDateTime().getYear() == year).toList();
        return earningList;
    }

    @Override
    public List<Earning> findAllEarningOfTheCurrentYear() {
        return findAllEarningOfTheYear(LocalDate.now().getYear());
    }

    @Override
    public List<Earning> findAllEarningOfTheMonth(int monthNumber, int year) {
        List<Earning> earningList = earningRepository.findAll().stream().filter(x -> ((int)x.getEarnedAt().toLocalDateTime().getYear() == year)).
                filter(y -> (y.getEarnedAt().toLocalDateTime().getMonth().getValue() == monthNumber)).
                toList();
        return earningList;
    }

    @Override
    public List<Earning> findAllEarningOfTheCurrentMonth() {
        return findAllEarningOfTheMonth(LocalDate.now().getMonth().getValue(), LocalDate.now().getYear());
    }

    @Override
    public void deleteSpending(long id) {
        spendingRepository.deleteById(id);
    }

    @Override
    public void deleteEarning(long id) {
        long chatId = earningRepository.getReferenceById(id).getBotUser().getId();
        earningRepository.deleteById(id);
        deleteEarningMarker = 0;
        if(earningRepository.findById(id).orElse(null) == null) {
            currentId = null;
            sendMessage(chatId, "Earning data with the id: " + id + " has been successfully deleted");
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }


    private boolean expectingButtonPressed(String callBackData, String expectingNameOfButton) {
//            Получаем идентификатор кнопки
        log.info("yesButtonPressed was called");
        if (callBackData.equals(expectingNameOfButton)) {
            return true;
        }
        return false;
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hi, " + name + "!" + ":blush:" + " Let's start working!" + ":computer:" + " What are we going to do right now?" + ":arrow_down:");
        List<String> buttonNames = List.of("/start", "/setearning", "/setearningbydate", "/settodayspending", "/setspending",
                "/findspending", "/findearning", "/deletespending", "/deleteearning", "/data", "/deletedata", "/help");
        sendMessageWithKeyboard(chatId, answer, buttonNames);
        log.info("Replied to user " + name);
    }

    private void sendMessageWithKeyboard(long chatId, String textToSend, List<String> buttonNames) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        if (!buttonNames.isEmpty()) {
            createKeyboardForRequest(buttonNames, sendMessage);
        }
        executeMessage(sendMessage);
    }

    private void sendMessageWithButtons(long chatId, String textToSend, List<List<String>> buttonNames) {
        log.info("sendMessageWithButtons was called");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        if (!buttonNames.isEmpty()) {
            createButtonsInMessage(sendMessage, buttonNames);
        }
        executeMessage(sendMessage);
    }

    private void createButtonsInMessage(SendMessage sendMessage, List<List<String>> buttonNames) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> lists = new ArrayList<>();
        for (List<String> list : buttonNames) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (String buttonName : list) {
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(buttonName);
                inlineKeyboardButton.setCallbackData(buttonName + "_BUTTON");
                buttons.add(inlineKeyboardButton);
            }
            lists.add(buttons);
        }
        markup.setKeyboard(lists);
        sendMessage.setReplyMarkup(markup);
    }

    private void sendMessage(long chatId, String textToSend) {
        log.info("sendMessage was called");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        executeMessage(sendMessage);
    }

    private void createKeyboardForRequest(List<String> buttonNames, SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        int counter = 0;
        for (String button : buttonNames) {
            if (counter == 3) {
                keyboardRows.add(row);
                counter = 0;
                row = new KeyboardRow();
            }
            row.add(button);
            counter += 1;
        }
        if (!row.isEmpty()) {
            keyboardRows.add(row);
        }
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
    }

    private void sendEditedMessage(long chatId, int messageId, String notificationMessage) {
        EditMessageText messageText = new EditMessageText();
        messageText.setChatId(String.valueOf(chatId));
        messageText.setText(notificationMessage);
        messageText.setMessageId((int) messageId);
        try {
            execute(messageText);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void executeMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    @Scheduled(cron = "${cron.scheduler}")
//    @Scheduled(cron = "0 * * * * *") - раз в минуту. Выше - раз в месяц
    private void sendAutoMessage() {
        List<AutomatedMessage> automatedMessageList = automatedMessageRepository.findAll();
        List<BotUser> botUsersList = botUserRepository.findAll();
        for (AutomatedMessage mes : automatedMessageList) {
            for (BotUser user : botUsersList) {
                sendMessage(user.getId(), mes.getAdMessage());
            }
        }
    }
}

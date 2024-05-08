package com.homeproject.controlbot.service;

import com.homeproject.controlbot.comparator.EarningDateComparator;
import com.homeproject.controlbot.configuration.BotConfig;
import com.homeproject.controlbot.entity.AutomatedMessage;
import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.entity.Earning;
import com.homeproject.controlbot.entity.Spending;
import com.homeproject.controlbot.enums.Marker;
import com.homeproject.controlbot.enums.TypeOfEarning;
import com.homeproject.controlbot.enums.TypeOfPurchase;
import com.homeproject.controlbot.exceptions.DataDoesNotExistException;
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
import java.time.Year;
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
    private TypeOfPurchase typeOfPurchase;

    private BigDecimal earnedSum;
    private String earningDate;
    private BigDecimal spentSum;
    private String spendingDate;
    private String shopName;
    private String descriptionOfPurchase;
    private Long currentId;
    private Marker marker;
    private int monthIndicator;
    private int dayIndicator;

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
                            marker = Marker.NONE;
                            log.info("Command /start was called by " + firstNameOfTheUser);
                            startCommandReceived(chatId, firstNameOfTheUser);
                            registerBotUser(update);
                            break;
                        case "/setearning":
                            log.info("Command /setearning was called by " + firstNameOfTheUser);
                            marker = Marker.NONE;
                            setEarningProcess(chatId);
                            break;
                        case "/setearningbydate":
                            log.info("Command /setearningbydate was called by " + firstNameOfTheUser);
                            marker = Marker.NONE;
                            setEarningByDateProcess(chatId);
                            break;
                        case "/settodayspending":
                            marker = Marker.NONE;
                            setSpendingProcess(chatId);
                            break;
                        case "/setspending":
                            marker = Marker.NONE;
                            setSpendingByDateProcess(chatId);
                            break;
                        case "/findSpending":
                            marker = Marker.NONE;
                            break;
                        case "/findearning":
                            marker = Marker.NONE;
                            List<List<String>> allTheButtons = List.of(List.of("All earnings"),
                                    List.of("Earnings of this year", "Earnings of the year ..."),
                                    List.of("Earnings of this month", "Earnings of the month ..."),
                                    List.of("Earnings of the day"));
                            sendMessageWithButtons(chatId, "Pick the search you want to proceed with: ", allTheButtons);
                            break;
                        case "/deletespending":
                            break;
                        case "/deleteearning":
                            marker = Marker.DELETE_EARNING;
                            sendMessage(chatId, "What is the ID of the earning you want to delete?");
                            break;
                        case "/data":
                            marker = Marker.NONE;
                            sendMessage(chatId, getBotUserInformation(receivedMessage).toString());
                            break;
                        case "/deletedata":
                            marker = Marker.NONE;
                            deleteBotUserInformation(update);
                            break;
                        case "/help":
                            marker = Marker.NONE;
                            sendMessage(chatId, HELP_TEXT);
                            break;
                        case "/register":
                            marker = Marker.NONE;
                            registerBotUser(update);
                            break;
                        default:
                            marker = Marker.NONE;
                            sendMessage(chatId, "Sorry, the command was not recognized");
                            break;
                    }
                }
            } else {
                switch (marker) {
                    case EARNING_DATE_IDENTIFICATOR:
                        log.info("EARNING_DATE_IDENTIFICATOR switch was reached");
                        String dataPattern = "\\d{2}-\\d{2}-\\d{4}";
                        if (Pattern.matches(dataPattern, receivedMessageText)) {
                            log.info("EARNING_DATE_IDENTIFICATOR switch was reached and pattern matches");
                                earningDate = receivedMessageText;
                                setEarningProcess(chatId);
//                            marker = Marker.SET_EARNING;
                        } else {
                            log.info("EARNING_DATE_IDENTIFICATOR switch was reached and pattern wasn't a match");
                            sendMessage(chatId, "The provided date has wrong format. You can try again by pressing /start.");
                            earningDate = null;
                            marker = Marker.NONE;
//                            marker = Marker.SET_EARNING;
                        }
                        break;
                    case SET_EARNING:
                        log.info("SET_EARNING switch was reached");
                        if (typeOfEarning != null) {
                            String numberPattern = "^\\d*\\.?\\d+$";
                            log.info("SET_EARNING switch was reached & typeOfEarning != null");
                            if (Pattern.matches(numberPattern, receivedMessageText)) {
                                log.info("EARNING_DATE_IDENTIFICATOR switch was reached and pattern was a match");
                                log.info("input text is: " + receivedMessageText);
                                earnedSum = new BigDecimal(receivedMessageText);
                                log.info("sum is: " + earnedSum);
                                if (earningDate == null) {
                                    setEarning(chatId, earnedSum);
                                } else {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                                    try {
                                        setEarningByDate(chatId, earnedSum, new Timestamp((dateFormat.parse(earningDate).getTime())));
                                    } catch (ParseException e) {
                                        log.error("An error occurred with parsing string to date: " + e.getMessage());
                                        sendMessage(chatId,"The provided date doesn't exist. Try again.");
                                        earnedSum = null;
                                        typeOfEarning = null;
                                        earningDate = null;
                                    }
                                }
                            } else {
                                earnedSum = null;
                                typeOfEarning = null;
                                log.error("The error occurred with earnedSum - format parsing.");
                                sendMessage(chatId, "Wrong format of the earned sum. You can try again pressing /start");
                            }
                        }
                        break;
                    case DELETE_EARNING:
                        log.info("Received info: " + receivedMessageText);
                        String idPattern = "^\\d+$";
                        if (Pattern.matches(idPattern, receivedMessageText)) {
                            log.info("Deleting id pattern has been approved: " + receivedMessageText);
                            currentId = Long.parseLong(receivedMessageText);
                            log.info("After parcing id: " + currentId);
                            deleteEarning(currentId, chatId);
                        } else {
                            marker = Marker.NONE;
                            currentId = null;
                        }
                        break;
                    case SOME_YEAR_EARNINGS:
                        log.info("Received info: " + receivedMessageText);
                        String yearPattern = "^\\d{4}$";
                        if (Pattern.matches(yearPattern, receivedMessageText)) {
                            int yearIndicator = Integer.parseInt(receivedMessageText);
                            if (monthIndicator == 0 && dayIndicator == 0) {
                                log.info("SOME_YEAR_EARNINGS & monthIndicator == 0 && dayIndicator == 0. Received message " + receivedMessageText);
                                sendMessage(chatId,
                                        findAllEarningOfTheYear(yearIndicator, chatId).toString());

                            } else if (monthIndicator != 0 && dayIndicator == 0) {
                                log.info("SOME_YEAR_EARNINGS & monthIndicator != 0 && dayIndicator == 0. Received message " + receivedMessageText);
                                sendMessage(chatId,
                                        findAllEarningOfTheMonth(monthIndicator, yearIndicator, chatId).toString());
                                monthIndicator = 0;
                            } else if (monthIndicator != 0 && dayIndicator != 0) {
                                log.info("SOME_YEAR_EARNINGS & monthIndicator != 0 && dayIndicator != 0. Received message " + receivedMessageText);
                                if (dateMatchesTheMonth(dayIndicator, monthIndicator, yearIndicator)){
                                    sendMessage(chatId,
                                            findAllEarningOfTheDay(dayIndicator, monthIndicator, yearIndicator, chatId).toString());
                                } else {
                                    sendMessage(chatId, "The date doesn't exist. Please, check the provided information " +
                                            "and try again by pressing /setearningbydate .");
                                }
                                monthIndicator = 0;
                                dayIndicator = 0;
                            }
                            marker = Marker.NONE;
                        } else {
                            marker = Marker.NONE;
                            log.info("Pattern doesn't match the year: " + receivedMessageText + ". For the user: " + currentId);
                            sendMessage(chatId, "The format of the year is not correct. " +
                                    "It must be XXXX. You can try again by pressing /start and choosing one of the options.");
                        }

                        break;
                    case SOME_MONTH_EARNING:
                        log.info("Received info: " + receivedMessageText);
                        String monthPattern = "^([1-9]|1[0-2])$";
                        if (Pattern.matches(monthPattern, receivedMessageText)) {
                            marker = Marker.SOME_YEAR_EARNINGS;
                            monthIndicator = Integer.parseInt(receivedMessageText);
                            sendMessage(chatId,
                                    "Please, enter the year: ");
                        } else {
                            marker = Marker.NONE;
                            log.info("Pattern doesn't match the month: " + receivedMessageText + ". For the user: " + currentId);
                            sendMessage(chatId, "The format of the month is not correct. " +
                                    "It must be 1-12. You can try again by pressing /start and choosing one of the options.");
                        }
                        break;
                    case SOME_DAY_EARNING:
                        log.info("Received info: " + receivedMessageText);
                        String dayPattern = "^([1-9]|[12][0-9]|3[01])$";
                        if (Pattern.matches(dayPattern, receivedMessageText)) {
                            marker = Marker.SOME_MONTH_EARNING;
                            dayIndicator = Integer.parseInt(receivedMessageText);
                            sendMessage(chatId,
                                    "Please, enter the month in format 1-12: ");
                        } else {
                            marker = Marker.NONE;
                            log.info("Pattern doesn't match the day: " + receivedMessageText + ". For the user: " + currentId);
                            sendMessage(chatId, "The format of the day is not correct. " +
                                    "It must be 1-31. You can try again by pressing /start and choosing one of the options.");
                        }
                        break;
                    case SHOP_NAME:
                        log.info("SHOP_NAME marker. Received info: " + receivedMessageText);
                        shopName = receivedMessageText;
                        setSpendingProcess(chatId);
                        break;
                    case DESCRIPTION_PURCHASE:
                        log.info("DESCRIPTION_PURCHASE marker. Received info: " + receivedMessageText);
                        descriptionOfPurchase = receivedMessageText;
                        setSpendingProcess(chatId);
                        break;
                    case SET_SPENDING:
                        log.info("SET_SPENDING switch was reached");
                        if (typeOfPurchase != null && shopName != null && descriptionOfPurchase != null) {
                            String numberPattern = "^\\d*\\.?\\d+$";
                            log.info("SET_SPENDING switch was reached & typeOfPurchase != null && shopName != null && descriptionOfPurchase != null");
                            if (Pattern.matches(numberPattern, receivedMessageText)) {
                                log.info("SET_SPENDING switch was reached and pattern was a match");
                                log.info("input text is: " + receivedMessageText);
                                spentSum = new BigDecimal(receivedMessageText);
                                log.info("sum is: " + spentSum);
                                if (spendingDate == null) {
                                    setSpending(chatId, spentSum);
                                } else {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                                    try {
                                        setSpendingByDate(chatId, spentSum, new Timestamp((dateFormat.parse(spendingDate).getTime())));
                                    } catch (ParseException e) {
                                        log.error("An error occurred with parsing string to date: " + e.getMessage());
                                        sendMessage(chatId,"The provided date doesn't exist. Try again.");
                                        spentSum = null;
                                        typeOfPurchase = null;
                                        spendingDate = null;
                                        descriptionOfPurchase = null;
                                        shopName = null;
                                    }
                                }
                            } else {
                                spentSum = null;
                                typeOfPurchase = null;
                                spendingDate = null;
                                descriptionOfPurchase = null;
                                shopName = null;
                                log.error("The error occurred with earnedSum - format parsing.");
                                sendMessage(chatId, "Wrong format of the spent sum. You can try again by pressing /start");
                            }
                        }
                        break;
                    case SPENDING_DATE_IDENTIFICATOR:
                        log.info("SPENDING_DATE_IDENTIFICATOR switch was reached");
                        String spendingDatePattern = "\\d{2}-\\d{2}-\\d{4}";
                        if (Pattern.matches(spendingDatePattern, receivedMessageText)) {
                            log.info("SPENDING_DATE_IDENTIFICATOR switch was reached and pattern matches");
                            spendingDate = receivedMessageText;
                            setSpendingProcess(chatId);
//                            marker = Marker.SET_EARNING;
                        } else {
                            log.info("SPENDING_DATE_IDENTIFICATOR switch was reached but pattern wasn't a match");
                            sendMessage(chatId, "The provided date has wrong format. You can try again by pressing /start.");
                            spendingDate = null;
                            marker = Marker.NONE;
//                            marker = Marker.SET_EARNING;
                        }
                        break;
                    default:
                        sendMessage(chatId, "Command was not correct");
                        break;
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
                    sendMessage(chatId, findAllEarning(chatId).toString());
                    break;
                case "Earnings of this year_BUTTON":
                    log.info("Earnings of this year_BUTTON" + chatId);
                    sendMessage(chatId, findAllEarningOfTheCurrentYear(chatId).toString());
                    break;
                case "Earnings of the year ..._BUTTON":
                    log.info("Earnings of the year_BUTTON" + chatId);
                    marker = Marker.SOME_YEAR_EARNINGS;
                    sendMessage(chatId, "Please, enter the correct year: ");
                    break;
                case "Earnings of this month_BUTTON":
                    log.info("Earnings of this month_BUTTON" + chatId);
                    sendMessage(chatId, findAllEarningOfTheCurrentMonth(chatId).toString());
                    break;
                case "Earnings of the month ..._BUTTON":
                    log.info("Earnings of the month ..._BUTTON" + chatId);
                    marker = Marker.SOME_MONTH_EARNING;
                    sendMessage(chatId, "Please, enter the correct month from 1-12: ");
                    break;
                case "Earnings of the day_BUTTON":
                    log.info("Earnings of the month ..._BUTTON" + chatId);
                    marker = Marker.SOME_DAY_EARNING;
                    sendMessage(chatId, "Please, enter the correct day from 1-31: ");
                    break;
                case "ELECTRONICS_BUTTON":
                    log.info("ELECTRONICS_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.ELECTRONICS;
                    setSpendingProcess(chatId);
                    break;
                case "TAXI_BUS_BUTTON":
                    log.info("TAXI_BUS_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.TAXI_BUS;
                    setSpendingProcess(chatId);
                    break;
                case "CAR_BUTTON":
                    log.info("CAR_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.CAR;
                    setSpendingProcess(chatId);
                    break;
                case "CAR_PARTS_BUTTON":
                    log.info("CAR_PARTS_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.CAR_PARTS;
                    setSpendingProcess(chatId);
                    break;
                case "CAR_REPAIR_BUTTON":
                    log.info("CAR_REPAIR_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.CAR_REPAIR;
                    setSpendingProcess(chatId);
                    break;
                case "FOOD_BUTTON":
                    log.info("FOOD_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.FOOD;
                    setEarningProcess(chatId);
                    break;
                case "SOFT_DRINK_BUTTON":
                    log.info("SOFT_DRINK_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.SOFT_DRINK;
                    setSpendingProcess(chatId);
                    break;
                case "ALCOHOL_BUTTON":
                    log.info("ALCOHOL_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.ALCOHOL;
                    setEarningProcess(chatId);
                    break;
                case "SWEETS_AND_COOKIES_BUTTON":
                    log.info("SWEETS_AND_COOKIES_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.SWEETS_AND_COOKIES;
                    setSpendingProcess(chatId);
                    break;
                case "RESTAURANT_BUTTON":
                    log.info("RESTAURANT_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.RESTAURANT;
                    setSpendingProcess(chatId);
                    break;
                case "RESORT_BUTTON":
                    log.info("RESORT_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.RESORT;
                    setSpendingProcess(chatId);
                    break;
                case "STATIONARY_BUTTON":
                    log.info("STATIONARY_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.STATIONARY;
                    setSpendingProcess(chatId);
                    break;
                case "HOME_STUFF_BUTTON":
                    log.info("HOME_STUFF_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.HOME_STUFF;
                    setSpendingProcess(chatId);
                    break;
                case "CLOTHES_BUTTON":
                    log.info("CLOTHES_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.CLOTHES;
                    setSpendingProcess(chatId);
                    break;
                case "FOOTWEAR_BUTTON":
                    log.info("FOOTWEAR_BUTTON" + chatId);
                    typeOfPurchase = TypeOfPurchase.FOOTWEAR;
                    setSpendingProcess(chatId);
                    break;
                default:
                    break;
            }
            if (!answerText.isEmpty()) {
                sendEditedMessage(chatId, (int) messageId, answerText);
            }
        }

    }

    private void registerBotUser(Update update) {
        Message message = update.getMessage();
        String userId = message.getChat().getUserName();
        log.info("Registration method was called by " + userId);
        if (botUserRepository.findById(message.getChatId()).isEmpty()) {
            log.info("there is no such user. He's needed to be saved");
            sendMessageWithButtons(message.getChatId(), "But first of all. Do you truly want to register and continue?", List.of(List.of("YES", "NO")));
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
        if (typeOfEarning == null) {
            log.info("setEarningProcess called and typeOfEarning == null, with chatId " + chatId);
            List<List<String>> earningTypeList = List.of(Arrays.stream(TypeOfEarning.values()).map(v -> v.toString()).toList());
            sendMessageWithButtons(chatId, "Pick the type of earning:", earningTypeList);
        } else {
            log.info("setEarningProcess called and typeOfEarning != null, with chatId " + chatId);
            if (earnedSum == null) {
                log.info("setEarningProcess called and sum == 0 " + chatId + ". Marker is: " + marker);
                marker = Marker.SET_EARNING;
                sendMessage(chatId, "What is the earned sum of money:");
            }
        }
    }

    @Override
    public void setEarningByDate(long chatId, BigDecimal sum, Timestamp date) {
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
            marker = Marker.NONE;
            earningDate = null;
        }
    }

    public void setEarningByDateProcess(long chatId) {
        log.info("setEarningByDateProcess method was called by " + chatId);
        marker = Marker.EARNING_DATE_IDENTIFICATOR;
        sendMessage(chatId, "Enter the date of this earning in format (DD-MM-YYYY):");
    }


    @Override
    public void setSpending(long chatId, BigDecimal spentSum) {
        log.info("setSpending has been started");
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        setSpendingByDate(chatId, spentSum, currentTime);
    }
    private void setSpendingProcess(long chatId){
        log.info("setSpendingProcess called with chatId " + chatId);
        if (typeOfPurchase == null) {
            log.info("setSpendingProcess called and typeOfPurchase == null, with chatId " + chatId);
            List<List<String>> purchaseTypeList = List.of(
                    List.of(TypeOfPurchase.CAR.toString(), TypeOfPurchase.CAR_PARTS.toString(), TypeOfPurchase.CAR_REPAIR.toString()),
                    List.of(TypeOfPurchase.ALCOHOL.toString(), TypeOfPurchase.FOOD.toString()),
                    List.of(TypeOfPurchase.RESTAURANT.toString(), TypeOfPurchase.SOFT_DRINK.toString(), TypeOfPurchase.SWEETS_AND_COOKIES.toString()),
                    List.of(TypeOfPurchase.CLOTHES.toString(), TypeOfPurchase.FOOTWEAR.toString()),
                    List.of(TypeOfPurchase.ELECTRONICS.toString(), TypeOfPurchase.HOME_STUFF.toString()),
                    List.of(TypeOfPurchase.STATIONARY.toString(), TypeOfPurchase.RESORT.toString(), TypeOfPurchase.TAXI_BUS.toString()));
            sendMessageWithButtons(chatId, "Pick the type of purchase:", purchaseTypeList);
        } else if(typeOfPurchase != null && shopName == null){
            log.info("setSpendingProcess called and typeOfPurchase != null && shopName == null, with chatId " + chatId);
            marker = Marker.SHOP_NAME;
            sendMessage(chatId, "Please, enter the name of the shop or other place you made the purchase at: ");
        }else if(typeOfPurchase != null && shopName != null && descriptionOfPurchase == null){
            log.info("setSpendingProcess called and typeOfPurchase != null && shopName != null && descriptionOfPurchase == null, with chatId " + chatId);
            marker = Marker.DESCRIPTION_PURCHASE;
            sendMessage(chatId, "Please, enter the description of the purchase. If you don't need to describe it," +
                    "you can just enter a dash: ");
        }
        else if(typeOfPurchase != null && shopName != null && descriptionOfPurchase != null && spentSum == null) {
            log.info("setSpendingProcess called and typeOfPurchase != null && shopName != null && descriptionOfPurchase == null, with chatId: " + chatId);
            if (spentSum == null) {
                log.info("setSpendingProcess was called and sum == 0 " + chatId + ". Marker is: " + marker);
                marker = Marker.SET_SPENDING;
                sendMessage(chatId, "What is the spent sum of money:");
            }
        } else{
            log.info("Wrong data in a process of executing the method. Clearing the data.");
            sendMessage(chatId, "There's been a mistake. If you want to retry, you can " +
                    "send /settodayspending or /setspending again.");
            spentSum = null;
            typeOfPurchase = null;
            spendingDate = null;
            descriptionOfPurchase = null;
            shopName = null;
        }
    }


    @Override
    public void setSpendingByDate(long chatId, BigDecimal sum, Timestamp date) {
        log.info("setSpendingByDate has been started");
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Spending spending = new Spending();
        spending.setRegisteredAt(currentTime);
        spending.setBotUser(botUserRepository.findById(chatId).orElse(null));
        spending.setSpentAt(date);
        spending.setTypeOfPurchase(typeOfPurchase);
        spending.setShopName(shopName);
        spending.setDescription(descriptionOfPurchase);
        spending.setSpendingSum(sum);
        spendingRepository.save(spending);
        shopName = null;
        descriptionOfPurchase = null;
        typeOfPurchase = null;
        spentSum = null;
        marker = Marker.NONE;
        spendingDate = null;
        if (!spendingRepository.findById(spending.getSpendingId()).isEmpty()){
            sendMessage(chatId, "The provided data about your recent spending has been successfully saved.");
        }
    }
    private void setSpendingByDateProcess(long chatId) {
        log.info("setSpendingByDateProcess method was called by " + chatId);
        marker = Marker.SPENDING_DATE_IDENTIFICATOR;
        sendMessage(chatId, "Enter the date of this spending in format (DD-MM-YYYY):");
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
    public List<Earning> findAllEarning(long chatId) {
        log.info("findAllEarning method was called by " + chatId);
        List<Earning> earningList = earningRepository.findAll().stream()
                .filter(earn -> earn.getBotUser().getId() == chatId)
                .sorted(new EarningDateComparator()).toList();
        return checkAndReturnListIfItIsNotEmpty(chatId, earningList);
    }

    //    @Override
    public List<Earning> findAllEarningOfTheYear(int year, long chatId) {
        log.info("findAllEarningOfTheYear method was called by " + chatId);
       List<Earning> earningList = findAllEarning(chatId).stream()
                .filter(x -> (int) x.getEarnedAt().toLocalDateTime().getYear() == year)
                .toList();
        return checkAndReturnListIfItIsNotEmpty(chatId, earningList);
    }

    @Override
    public List<Earning> findAllEarningOfTheCurrentYear(long chatId) {
        log.info("findAllEarningOfTheCurrentYear method was called by " + chatId);
        List<Earning> earningList = findAllEarningOfTheYear(LocalDate.now().getYear(), chatId);
        return checkAndReturnListIfItIsNotEmpty(chatId, earningList);
    }

    @Override
    public List<Earning> findAllEarningOfTheMonth(int monthNumber, int year, long chatId) {
        log.info("findAllEarningOfTheMonth method was called by " + chatId);
        List<Earning> earningList = findAllEarning(chatId).stream()
                .filter(x -> ((int) x.getEarnedAt().toLocalDateTime().getYear() == year))
                .filter(y -> (y.getEarnedAt().toLocalDateTime().getMonth().getValue() == monthNumber))
                .toList();
        return checkAndReturnListIfItIsNotEmpty(chatId, earningList);
    }

    public List<Earning> findAllEarningOfTheDay(int day, int monthNumber, int year, long chatId) {
        log.info("findAllEarningOfTheDay method was called by " + chatId);
        List<Earning> earningList = findAllEarning(chatId).stream()
                .filter(x -> ((int) x.getEarnedAt().toLocalDateTime().getYear() == year))
                .filter(y -> (y.getEarnedAt().toLocalDateTime().getMonth().getValue() == monthNumber))
                .filter(y -> (y.getEarnedAt().toLocalDateTime().getDayOfMonth() == day))
                .toList();
        return checkAndReturnListIfItIsNotEmpty(chatId, earningList);
    }

    @Override
    public List<Earning> findAllEarningOfTheCurrentMonth(long chatId) {
        log.info("findAllEarningOfTheCurrentMonth method was called by " + chatId);
        List<Earning> earningList = findAllEarningOfTheMonth(LocalDate.now().getMonth().getValue(), LocalDate.now().getYear(), chatId);
        return checkAndReturnListIfItIsNotEmpty(chatId, earningList);
    }

    @Override
    public void deleteSpending(long id) {
//        log.info("findAllEarningOfTheCurrentMonth method was called by " + chatId);
        spendingRepository.deleteById(id);
    }

    @Override
    public void deleteEarning(long id, long chatIdCheck) {
        log.info("deleteEarning method was called by " + chatIdCheck + ". Deleting of the earning by id: " + id);
        Earning earningToDelete = earningRepository.findById(id).orElse(null);
        long chatId = 0;
        if (earningToDelete != null) {
            chatId = earningToDelete.getBotUser().getId();
        }
        if (chatId == chatIdCheck) {
            earningRepository.deleteById(id);
//            deleteEarningMarker = 0;
            marker = Marker.NONE;
            if (earningRepository.findById(id).orElse(null) == null) {
                currentId = null;
                sendMessage(chatId, "Earning data with the id: " + id + " has been successfully deleted");
            }
        }
        marker = Marker.NONE;
//        deleteEarningMarker = 0;
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
    private boolean dateMatchesTheMonth(int day, int month, int year){
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                if (day > 0 && day <= 31){
                    return true;
                }
                break;
            case 2:
                if (day > 0 && day <= 29){
                    if (day == 29 && Year.of(year).isLeap()){
                        return true;
                    } else if (day == 29 && !Year.of(year).isLeap()) {
                        return false;
                    }
                    return true;
                }
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                if (day > 0 && day <= 30){
                    return true;
                }
                break;
            default:

        }
        return true;
    }

    private <E> List<E> checkAndReturnListIfItIsNotEmpty(long chatId, List<E> list){
        if (list.isEmpty()){
            sendMessage(chatId, "Earning list you are looking for doesn't exist");
//            throw new DataDoesNotExistException("Earning list you are looking for doesn't exist");
            log.error("Earning list you are looking for doesn't exist");
        }
        return list;
    }
}

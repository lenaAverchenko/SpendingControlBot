package com.homeproject.controlbot.service;

import com.homeproject.controlbot.configuration.BotConfig;
import com.homeproject.controlbot.entity.AutomatedMessage;
import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.enums.Marker;
import com.homeproject.controlbot.enums.TypeOfEarning;
import com.homeproject.controlbot.enums.TypeOfPurchase;
import com.homeproject.controlbot.helper.ButtonAndListCreator;
import com.homeproject.controlbot.helper.ProfitCalculator;
import com.homeproject.controlbot.repository.AutomatedMessageRepository;
import com.homeproject.controlbot.repository.BotUserRepository;
import com.homeproject.controlbot.repository.EarningRepository;
import com.homeproject.controlbot.repository.SpendingRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.Data;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;


@Slf4j
@Service
@Data
public class SpendingControlBotServiceImpl extends TelegramLongPollingBot implements SpendingControlBotService {

    @Autowired
    private BotUserRepository botUserRepository;
    @Autowired
    private EarningRepository earningRepository;
    @Autowired
    private EarningService earningService;
    @Autowired
    private SpendingRepository spendingRepository;
    @Autowired
    private SpendingService spendingService;
    @Autowired
    private AutomatedMessageRepository automatedMessageRepository;
    private TypeOfEarning typeOfEarning;
    private TypeOfPurchase typeOfPurchase;
    private ButtonAndListCreator buttonAndListCreator = new ButtonAndListCreator();
    @Autowired
    private ProfitCalculator profitCalculator;
    private BigDecimal earnedSum;
    private String earningDate;
    private BigDecimal spentSum;
    private String spendingDate;
    private String startDateProfit;
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
            "Type /calculateprofit to calculate profit for a particular period\n\n" +
            "Type /deletespending to delete the wrong spending from database\n\n" +
            "Type /deleteearning to delete the wrong earning from database\n\n" +
            "Type /data to get info about the stored data from your account\n\n" +
            "Type /deletedata to delete stored info about your account\n\n" +
            "Type /help to see this message again.";

    //    private SpendingControlBotServiceImpl(BotConfig botConfig) {
    public SpendingControlBotServiceImpl(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "start working with spendingControlBot"));
        listOfCommands.add(new BotCommand("/setearning", "set information about earning"));
        listOfCommands.add(new BotCommand("/setearningbydate", "set information about earning of the particular day"));
        listOfCommands.add(new BotCommand("/settodayspending", "set information about Today's spending"));
        listOfCommands.add(new BotCommand("/setspending", "set information about some day's spending"));
        listOfCommands.add(new BotCommand("/findspending", "find spending according to your preferences"));
        listOfCommands.add(new BotCommand("/findearning", "find earning according to your preferences"));
        listOfCommands.add(new BotCommand("/calculateprofit", "to calculate profit for the particular period of time"));
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
                        case "/calculateprofit":
                            List<List<String>> listOfOptions = buttonAndListCreator.createListOfLists(List.of("Profit of the current month",
                                    "Profit of the optional month", "Profit of the current year", "Profit of the optional year",
                                    "Profit for selected period"));
                            sendMessageWithButtons(chatId, "Please, choose the period to calculate your profit:", listOfOptions);
                            break;
                        case "/findspending":
                            marker = Marker.NONE;
                            List<List<String>> allTheSpendingButtons = buttonAndListCreator.createListOfLists(List.of("All info about my spending",
                                    "Money spent this year", "Money spent the year ...", "Money spent this month",
                                    "Money spent the month ...", "Money spent today", "Money spent the day ..."));
                            sendMessageWithButtons(chatId, "Pick the search you want to proceed with: ", allTheSpendingButtons);
                            break;
                        case "/findearning":
                            marker = Marker.NONE;
                            List<List<String>> allTheButtons = buttonAndListCreator.createListOfLists(List.of(
                                    "All earnings", "Earnings of this year", "Earnings of the year ...",
                                    "Earnings of this month", "Earnings of the month ...", "Earnings of the day"));
                            sendMessageWithButtons(chatId, "Pick the search you want to proceed with: ", allTheButtons);
                            break;
                        case "/deletespending":
                            marker = Marker.DELETE_SPENDING;
                            sendMessage(chatId, "What is the ID of the spending you want to delete?");
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
                        dateIdentifierReceived(chatId, receivedMessageText);
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
                                        sendMessage(chatId, "The provided date doesn't exist. Try again.");
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
                    case DELETE_SPENDING:
                        deleteEarningOrSpendingIdentifierReceived(chatId, receivedMessageText);
                        break;
                    case SOME_YEAR_EARNINGS:
                    case SOME_YEAR_SPENDING:
                        someYearIdentifierReceived(chatId, receivedMessageText);
                        break;
                    case SOME_MONTH_EARNING:
                    case SOME_MONTH_SPENDING:
                        someMonthIdentifierReceived(chatId, receivedMessageText);
                        break;
                    case SOME_DAY_EARNING:
                    case SOME_DAY_SPENDING:
                        someDayIdentifierReceived(chatId, receivedMessageText);
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
                                        sendMessage(chatId, "The provided date doesn't exist. Try again.");
                                        clearTheSpendingData();
                                    }
                                }
                            } else {
                                clearTheSpendingData();
                                log.error("The error occurred with earnedSum - format parsing.");
                                sendMessage(chatId, "Wrong format of the spent sum. You can try again by pressing /start");
                            }
                        }
                        break;
                    case SPENDING_DATE_IDENTIFICATOR:
                        log.info("SPENDING_DATE_IDENTIFICATOR switch was reached");
                        dateIdentifierReceived(chatId, receivedMessageText);
                        break;
                    case START_DATE_PROFIT:
                        log.info("START_DATE_PROFIT switch was reached");
                        dateIdentifierReceived(chatId, receivedMessageText);
                        break;
                    case END_DATE_PROFIT:
                        log.info("END_DATE_PROFIT switch was reached");
                        dateIdentifierReceived(chatId, receivedMessageText);
                        break;
                    case MONTH_PROFIT:
                        log.info("MONTH_PROFIT marker. Received info: " + receivedMessageText);
                        someMonthIdentifierReceived(chatId, receivedMessageText);
                        break;
                    case YEAR_PROFIT:
                        log.info("Received info: " + receivedMessageText);
                        String yearProfitPattern = "^\\d{4}$";
                        if (Pattern.matches(yearProfitPattern, receivedMessageText)) {
                            int yearIndicator = Integer.parseInt(receivedMessageText);
                            if (monthIndicator == 0) {
                                log.info("YEAR_PROFIT & monthIndicator == 0. Received message " + receivedMessageText);
                                calculateProfitOfTheYear(chatId, yearIndicator);
                                yearIndicator = 0;
                            } else if (monthIndicator != 0) {
                                log.info("YEAR_PROFIT & monthIndicator != 0. Received message " + receivedMessageText);
                                calculateProfitOfTheMonth(chatId, yearIndicator, monthIndicator);
                                monthIndicator = 0;
                            }
                            marker = Marker.NONE;
                        } else {
                            marker = Marker.NONE;
                            log.info("Pattern doesn't match the year: " + receivedMessageText + ". For the user: " + currentId);
                            sendMessage(chatId, "The format of the year is not correct. " +
                                    "It must be XXXX. You can try again by pressing /start and choosing one of the options.");
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
                    setMarkerAndEarningProcess(chatId, callBackData, TypeOfEarning.SALARY);
                    break;
                case "CHILD_SUPPORT_BUTTON":
                    setMarkerAndEarningProcess(chatId, callBackData, TypeOfEarning.CHILD_SUPPORT);
                    break;
                case "PRISE_BUTTON":
                    setMarkerAndEarningProcess(chatId, callBackData, TypeOfEarning.PRISE);
                    break;
                case "GIFT_BUTTON":
                    setMarkerAndEarningProcess(chatId, callBackData, TypeOfEarning.GIFT);
                    break;
                case "All earnings_BUTTON":
                    log.info("All earnings_BUTTON" + chatId);
                    sendMessage(chatId, checkAndReturnListIfItIsNotEmpty(chatId,
                            earningService.findAllEarning(chatId)).toString());
                    break;
                case "Earnings of this year_BUTTON":
                    log.info("Earnings of this year_BUTTON" + chatId);
                    sendMessage(chatId, checkAndReturnListIfItIsNotEmpty(chatId,
                            earningService.findAllEarningOfTheCurrentYear(chatId)).toString());
                    break;
                case "Earnings of the year ..._BUTTON":
                    log.info("Earnings of the year_BUTTON" + chatId);
                    marker = Marker.SOME_YEAR_EARNINGS;
                    sendMessage(chatId, "Please, enter the correct year: ");
                    break;
                case "Earnings of this month_BUTTON":
                    log.info("Earnings of this month_BUTTON" + chatId);
                    sendMessage(chatId, checkAndReturnListIfItIsNotEmpty(chatId,
                            earningService.findAllEarningOfTheCurrentMonth(chatId)).toString());
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
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.ELECTRONICS);
                    break;
                case "TAXI_BUS_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.TAXI_BUS);
                    break;
                case "CAR_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.CAR);
                    break;
                case "CAR_PARTS_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.CAR_PARTS);
                    break;
                case "CAR_REPAIR_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.CAR_REPAIR);
                    break;
                case "FOOD_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.FOOD);
                    break;
                case "SOFT_DRINK_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.SOFT_DRINK);
                    break;
                case "ALCOHOL_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.ALCOHOL);
                    break;
                case "SWEETS_AND_COOKIES_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.SWEETS_AND_COOKIES);
                    break;
                case "RESTAURANT_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.RESTAURANT);
                    break;
                case "RESORT_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.RESORT);
                    break;
                case "STATIONARY_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.STATIONARY);
                    break;
                case "HOME_STUFF_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.HOME_STUFF);
                    break;
                case "CLOTHES_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.CLOTHES);
                    break;
                case "FOOTWEAR_BUTTON":
                    setMarkerAndSpendingProcess(chatId, callBackData, TypeOfPurchase.FOOTWEAR);
                    break;
                case "All info about my spending_BUTTON":
                    log.info("All info about my spending_BUTTON " + chatId);
                    sendMessage(chatId, checkAndReturnListIfItIsNotEmpty(chatId,
                            spendingService.findAllSpending(chatId)).toString());
                    break;
                case "Money spent this year_BUTTON":
                    log.info("Money spent this year_BUTTON " + chatId);
                    sendMessage(chatId, checkAndReturnListIfItIsNotEmpty(chatId,
                            spendingService.findAllSpendingOfTheCurrentYear(chatId)).toString());
                    break;
                case "Money spent the year ..._BUTTON":
                    log.info("Money spent the year ..._BUTTON " + chatId);
                    marker = Marker.SOME_YEAR_SPENDING;
                    sendMessage(chatId, "Please, enter the correct year: ");
                    break;
                case "Money spent this month_BUTTON":
                    log.info("Money spent this month_BUTTON " + chatId);
                    sendMessage(chatId, checkAndReturnListIfItIsNotEmpty(chatId,
                            spendingService.findAllSpendingOfTheCurrentMonth(chatId)).toString());
                    break;
                case "Money spent the month ..._BUTTON":
                    log.info("Money spent the month ..._BUTTON " + chatId);
                    marker = Marker.SOME_MONTH_SPENDING;
                    sendMessage(chatId, "Please, enter the correct month from 1-12: ");
                    break;
                case "Money spent today_BUTTON":
                    log.info("Money spent today_BUTTON " + chatId);
                    sendMessage(chatId, checkAndReturnListIfItIsNotEmpty(chatId,
                            spendingService.findAllSpendingOfTheCurrentDay(chatId)).toString());
                    break;
                case "Money spent the day ..._BUTTON":
                    log.info("Money spent the day ..._BUTTON " + chatId);
                    marker = Marker.SOME_DAY_SPENDING;
                    sendMessage(chatId, "Please, enter the correct day from 1-31: ");
                    break;
                case "Profit of the current month_BUTTON":
                    log.info("Profit of the current month_BUTTON " + chatId);
                    calculateProfitOfTheCurrentMonth(chatId);
                    break;
                case "Profit of the optional month_BUTTON":
                    log.info("Profit of the optional month_BUTTON " + chatId);
                    marker = Marker.MONTH_PROFIT;
                    sendMessage(chatId, "Please, enter the month, when you want to calculate profit: ");
                    break;
                case "Profit of the current year_BUTTON":
                    log.info("Profit of the current year_BUTTON " + chatId);
                    calculateProfitOfTheCurrentYear(chatId);
                    break;
                case "Profit of the optional year_BUTTON":
                    log.info("Profit of the optional year_BUTTON " + chatId);
                    marker = Marker.YEAR_PROFIT;
                    sendMessage(chatId, "Please, enter the year, when you want to calculate profit: ");
                    break;
                case "Profit for selected period_BUTTON":
                    log.info("Profit for selected period_BUTTON " + chatId);
                    marker = Marker.START_DATE_PROFIT;
                    sendMessage(chatId, "Please, enter the initial date in format DD-MM-YYYY to start the calculating process: ");
                    break;
                default:
                    break;
            }
            if (answerText != null) {
                sendEditedMessage(chatId, (int) messageId, answerText);
            }
        }
    }

    public void registerBotUser(Update update) {
        Message message = update.getMessage();
        String userId = message.getChat().getUserName();
        log.info("Registration method was called by " + userId);
        if (botUserRepository.findById(message.getChatId()).isEmpty()) {
            log.info("there is no such user. He's needed to be saved");
            sendMessageWithButtons(message.getChatId(), "But first of all. Do you truly want to register and continue?", List.of(List.of("YES", "NO")));
            log.info("registration question was asked");
        }
    }

    public void setMarkerAndEarningProcess(long chatId, String message, TypeOfEarning typeToSet) {
        log.info(message + ": " + chatId);
        typeOfEarning = typeToSet;
        setEarningProcess(chatId);
    }

    public void setMarkerAndSpendingProcess(long chatId, String message, TypeOfPurchase typeToSet) {
        log.info(message + chatId);
        typeOfPurchase = typeToSet;
        setSpendingProcess(chatId);
    }

    public String registrationPermittedAct(Message message) {
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

    public void someYearIdentifierReceived(long chatId, String receivedMessageText) {
        log.info("Received info: " + receivedMessageText);
        String yearPattern = "^\\d{4}$";
        if (Pattern.matches(yearPattern, receivedMessageText)) {
            int yearIndicator = Integer.parseInt(receivedMessageText);
            if (monthIndicator == 0 && dayIndicator == 0) {
                if (marker == Marker.SOME_YEAR_EARNINGS) {
                    log.info("SOME_YEAR_EARNINGS & monthIndicator == 0 && dayIndicator == 0. Received message " + receivedMessageText);
                    sendMessage(chatId,
                            checkAndReturnListIfItIsNotEmpty(chatId,
                                    earningService.findAllEarningOfTheYear(yearIndicator, chatId)).toString());
                } else if (marker == Marker.SOME_YEAR_SPENDING) {
                    log.info("SOME_YEAR_SPENDING & monthIndicator == 0 && dayIndicator == 0. Received message " + receivedMessageText);
                    sendMessage(chatId, checkAndReturnListIfItIsNotEmpty(chatId,
                            spendingService.findAllSpendingOfTheYear(yearIndicator, chatId)).toString());
                }
            } else if (monthIndicator != 0 && dayIndicator == 0) {
                if (marker == Marker.SOME_YEAR_EARNINGS) {
                    log.info("SOME_YEAR_EARNINGS & monthIndicator != 0 && dayIndicator == 0. Received message " + receivedMessageText);
                    sendMessage(chatId, checkAndReturnListIfItIsNotEmpty(chatId,
                            earningService.findAllEarningOfTheMonth(monthIndicator, yearIndicator, chatId)).toString());
                } else if (marker == Marker.SOME_YEAR_SPENDING) {
                    log.info("SOME_YEAR_SPENDING & monthIndicator != 0 && dayIndicator == 0. Received message " + receivedMessageText);
                    sendMessage(chatId, checkAndReturnListIfItIsNotEmpty(chatId,
                            spendingService.findAllSpendingOfTheMonth(monthIndicator, yearIndicator, chatId)).toString());
                }
                monthIndicator = 0;
            } else if (monthIndicator != 0 && dayIndicator != 0) {
                if (dateMatchesTheMonth(dayIndicator, monthIndicator, yearIndicator)) {
                    if (marker == Marker.SOME_YEAR_EARNINGS) {
                        log.info("SOME_YEAR_EARNINGS & monthIndicator != 0 && dayIndicator != 0. Received message " + receivedMessageText);
                        sendMessage(chatId,
                                checkAndReturnListIfItIsNotEmpty(chatId, earningService.findAllEarningOfTheDay(dayIndicator, monthIndicator, yearIndicator, chatId)).toString());
                    } else if (marker == Marker.SOME_YEAR_SPENDING) {
                        log.info("SOME_YEAR_SPENDING & monthIndicator != 0 && dayIndicator != 0. Received message " + receivedMessageText);
                        sendMessage(chatId, checkAndReturnListIfItIsNotEmpty(chatId,
                                spendingService.findAllSpendingOfTheDay(dayIndicator, monthIndicator, yearIndicator, chatId)).toString());
                    }
                } else {
                    sendMessage(chatId, "The date doesn't exist. Please, check the provided information " +
                            "and try again by pressing /start .");
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

    }

    public void someMonthIdentifierReceived(long chatId, String receivedMessageText) {
        log.info("Received info: " + receivedMessageText);
        String monthPattern = "^([1-9]|1[0-2])$";
        if (Pattern.matches(monthPattern, receivedMessageText)) {
            if (marker == Marker.SOME_MONTH_EARNING) {
                marker = Marker.SOME_YEAR_EARNINGS;
            } else if (marker == Marker.SOME_MONTH_SPENDING) {
                marker = Marker.SOME_YEAR_SPENDING;
            } else if (marker == Marker.MONTH_PROFIT) {
                marker = Marker.YEAR_PROFIT;
            }
            monthIndicator = Integer.parseInt(receivedMessageText);
            sendMessage(chatId,
                    "Please, enter the year to go to other steps: ");
        } else {
            marker = Marker.NONE;
            log.info("Pattern doesn't match the month: " + receivedMessageText + ". For the user: " + currentId);
            sendMessage(chatId, "The format of the month is not correct. " +
                    "It must be 1-12. You can try again by pressing /start and choosing one of the options.");
        }
    }

    public void someDayIdentifierReceived(long chatId, String receivedMessageText) {
        log.info("Received info: " + receivedMessageText);
        String dayPattern = "^([1-9]|[12][0-9]|3[01])$";
        if (Pattern.matches(dayPattern, receivedMessageText)) {
            if (marker == Marker.SOME_DAY_SPENDING) {
                marker = Marker.SOME_MONTH_SPENDING;
            } else if (marker == Marker.SOME_DAY_EARNING) {
                marker = Marker.SOME_MONTH_EARNING;
            }
            dayIndicator = Integer.parseInt(receivedMessageText);
            sendMessage(chatId, "Please, enter the month in format 1-12: ");
        } else {
            marker = Marker.NONE;
            log.info("Pattern doesn't match the day: " + receivedMessageText + ". For the user: " + currentId);
            sendMessage(chatId, "The format of the day is not correct. " +
                    "It must be 1-31. You can try again by pressing /start and choosing one of the options.");
        }
    }

    public void deleteEarningOrSpendingIdentifierReceived(long chatId, String receivedMessageText) {
        log.info("Received info: " + receivedMessageText);
        String idPattern = "^\\d+$";
        if (Pattern.matches(idPattern, receivedMessageText)) {
            log.info("Deleting id pattern has been approved: " + receivedMessageText);
            currentId = Long.parseLong(receivedMessageText);
            log.info("After parcing id: " + currentId);
            if (marker == Marker.DELETE_SPENDING) {
                sendMessage(chatId, spendingService.deleteSpending(currentId, chatId));
            } else if (marker == Marker.DELETE_EARNING) {
                sendMessage(chatId, earningService.deleteEarning(currentId, chatId));
            }
        }
        marker = Marker.NONE;
        currentId = null;
    }


    public void dateIdentifierReceived(long chatId, String receivedMessageText) {
        String dataPattern = "\\d{2}-\\d{2}-\\d{4}";
        if (Pattern.matches(dataPattern, receivedMessageText)) {
            if (marker == Marker.EARNING_DATE_IDENTIFICATOR) {
                log.info("EARNING_DATE_IDENTIFICATOR switch was reached and pattern matches");
                earningDate = receivedMessageText;
                setEarningProcess(chatId);
            } else if (marker == Marker.SPENDING_DATE_IDENTIFICATOR) {
                log.info("SPENDING_DATE_IDENTIFICATOR switch was reached and pattern matches");
                spendingDate = receivedMessageText;
                setSpendingProcess(chatId);
            } else if (marker == Marker.END_DATE_PROFIT) {
                log.info("END_DATE_PROFIT switch was reached and pattern matches");
                marker = Marker.NONE;
                calculateProfitOfTheSelectedPeriod(chatId, startDateProfit, receivedMessageText);
            } else if (marker == Marker.START_DATE_PROFIT) {
                log.info("START_DATE_PROFIT switch was reached and pattern matches");
                startDateProfit = receivedMessageText;
                marker = Marker.END_DATE_PROFIT;
                sendMessage(chatId, "Please, enter the initial date in format DD-MM-YYYY to start the calculating process: ");
            }
        } else {
            log.info(marker.toString() + " switch was reached and pattern wasn't a match");
            sendMessage(chatId, "The provided date has wrong format. You can try again by pressing /start.");
            earningDate = null;
            spendingDate = null;
            startDateProfit = null;
            marker = Marker.NONE;
        }
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

    public BigDecimal getAndClearCurrentProfit() {
        BigDecimal numberToReturn = profitCalculator.getCurrentProfit();
        profitCalculator.setCurrentProfit(null);
        return numberToReturn;
    }

    public BigDecimal calculateProfit(long chatId, BigDecimal earnedSum, BigDecimal spentSum) {
        sendMessage(chatId, profitCalculator.calculateProfit(chatId, earnedSum, spentSum));
        return getAndClearCurrentProfit();
    }

    public BigDecimal calculateProfitOfTheYear(long chatId, int year) {
        log.info("calculateProfitOfTheYear started");
        sendMessage(chatId, profitCalculator.calculateProfitOfTheYear(chatId, year));
        return getAndClearCurrentProfit();
    }

    public BigDecimal calculateProfitOfTheCurrentYear(long chatId) {
        sendMessage(chatId, profitCalculator.calculateProfitOfTheCurrentYear(chatId));
        return getAndClearCurrentProfit();
    }

    public BigDecimal calculateProfitOfTheMonth(long chatId, int year, int month) {
        sendMessage(chatId, profitCalculator.calculateProfitOfTheMonth(chatId, year, month));
        return getAndClearCurrentProfit();
    }

    public BigDecimal calculateProfitOfTheCurrentMonth(long chatId) {
        sendMessage(chatId, profitCalculator.calculateProfitOfTheCurrentMonth(chatId));
        return getAndClearCurrentProfit();
    }

    public BigDecimal calculateProfitOfTheSelectedPeriod(long chatId, String startDate, String endDate) {
        sendMessage(chatId, profitCalculator.calculateProfitOfTheSelectedPeriod(chatId, startDate, endDate));
        return getAndClearCurrentProfit();
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
            List<List<String>> earningTypeList = buttonAndListCreator.createListOfLists(Arrays.stream(TypeOfEarning.values())
                    .map(Enum::toString).toList());
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
        sendMessage(chatId, earningService.saveEarningByDate(chatId, date, botUserRepository.findById(chatId).orElse(null),
                typeOfEarning, earnedSum));
        earnedSum = null;
        marker = Marker.NONE;
        earningDate = null;
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

    public void setSpendingProcess(long chatId) {
        log.info("setSpendingProcess called with chatId " + chatId);
        if (typeOfPurchase == null) {
            log.info("setSpendingProcess called and typeOfPurchase == null, with chatId " + chatId);
            List<List<String>> purchaseTypeList = buttonAndListCreator.createListOfLists(Arrays.stream(TypeOfPurchase.values())
                    .map(Enum::toString).toList());
            sendMessageWithButtons(chatId, "Pick the type of purchase:", purchaseTypeList);
        } else if (typeOfPurchase != null && shopName == null) {
            log.info("setSpendingProcess called and typeOfPurchase != null && shopName == null, with chatId " + chatId);
            marker = Marker.SHOP_NAME;
            sendMessage(chatId, "Please, enter the name of the shop or other place you made the purchase at: ");
        } else if (typeOfPurchase != null && shopName != null && descriptionOfPurchase == null) {
            log.info("setSpendingProcess called and typeOfPurchase != null && shopName != null && descriptionOfPurchase == null, with chatId " + chatId);
            marker = Marker.DESCRIPTION_PURCHASE;
            sendMessage(chatId, "Please, enter the description of the purchase. If you don't need to describe it," +
                    "you can just enter a dash: ");
        } else if (typeOfPurchase != null && shopName != null && descriptionOfPurchase != null && spentSum == null) {
            log.info("setSpendingProcess called and typeOfPurchase != null && shopName != null && descriptionOfPurchase == null, with chatId: " + chatId);
            if (spentSum == null) {
                log.info("setSpendingProcess was called and sum == 0 " + chatId + ". Marker is: " + marker);
                marker = Marker.SET_SPENDING;
                sendMessage(chatId, "What is the spent sum of money:");
            }
        } else {
            log.info("Wrong data in a process of executing the method. Clearing the data.");
            sendMessage(chatId, "There's been a mistake. If you want to retry, you can " +
                    "send /settodayspending or /setspending again.");
            clearTheSpendingData();
        }
    }

    public void clearTheSpendingData() {
        spentSum = null;
        typeOfPurchase = null;
        spendingDate = null;
        descriptionOfPurchase = null;
        shopName = null;
    }


    @Override
    public void setSpendingByDate(long chatId, BigDecimal sum, Timestamp date) {
        log.info("setSpendingByDate has been started");
        sendMessage(chatId,
                spendingService.saveSpendingByDate(chatId, botUserRepository.findById(chatId).orElse(null),
                        typeOfPurchase, shopName, descriptionOfPurchase, sum, date));
        clearTheSpendingData();
        marker = Marker.NONE;
    }

    public void setSpendingByDateProcess(long chatId) {
        log.info("setSpendingByDateProcess method was called by " + chatId);
        marker = Marker.SPENDING_DATE_IDENTIFICATOR;
        sendMessage(chatId, "Enter the date of this spending in format (DD-MM-YYYY):");
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    public void startCommandReceived(long chatId, String name) {
        log.info("startCommandReceived method has been started.");
        String answer = EmojiParser.parseToUnicode("Hi, " + name + "!" + ":blush:" + " Let's start working!" + ":computer:" + " What are we going to do right now?" + ":arrow_down:");
        List<String> buttonNames = List.of("/start", "/setearning", "/setearningbydate", "/settodayspending", "/setspending",
                "/findspending", "/findearning", "/calculateprofit", "/deletespending", "/deleteearning", "/data", "/deletedata", "/help");
        sendMessageWithKeyboard(chatId, answer, buttonNames);
        log.info("Replied to user " + name);
    }

    public void sendMessageWithKeyboard(long chatId, String textToSend, List<String> buttonNames) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        if (!buttonNames.isEmpty()) {
            buttonAndListCreator.createKeyboardForRequest(buttonNames, sendMessage);
        }
        executeMessage(sendMessage);
    }

    public void sendMessageWithButtons(long chatId, String textToSend, List<List<String>> buttonNames) {
        log.info("sendMessageWithButtons was called");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        if (!buttonNames.isEmpty()) {
            buttonAndListCreator.createButtonsInMessage(sendMessage, buttonNames);
        }
        executeMessage(sendMessage);
    }

    public void sendMessage(long chatId, String textToSend) {
        log.info("sendMessage was called");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        executeMessage(sendMessage);
    }

    public void sendEditedMessage(long chatId, int messageId, String notificationMessage) {
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

    public void executeMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    @Scheduled(cron = "${cron.scheduler}")
    public void sendAutoMessage() {
        List<AutomatedMessage> automatedMessageList = automatedMessageRepository.findAll();
        List<BotUser> botUsersList = botUserRepository.findAll();
        for (AutomatedMessage mes : automatedMessageList) {
            for (BotUser user : botUsersList) {
                sendMessage(user.getId(), mes.getAdMessage());
            }
        }
    }

    public boolean dateMatchesTheMonth(int day, int month, int year) {
        boolean toReturn = false;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                if (day > 0 && day <= 31) {
                    toReturn = true;
                }
                break;
            case 2:
                if (day > 0 && day <= 29) {
                    toReturn = true;
                    if (day == 29 && !Year.of(year).isLeap()) {
                        toReturn = false;
                    }
                }
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                if (day > 0 && day <= 30) {
                    toReturn = true;
                }
                break;
            default:
                break;
        }
        return toReturn;
    }

    public <E> List<E> checkAndReturnListIfItIsNotEmpty(long chatId, List<E> list) {
        if (list.isEmpty()) {
            String textToSend = "The data you are looking for doesn't exist.";
            sendMessage(chatId, textToSend);
//            throw new DataDoesNotExistException("The data you are looking for doesn't exist");
            log.error(textToSend + " For user with id: " + chatId);
        }
        return list;
    }
}
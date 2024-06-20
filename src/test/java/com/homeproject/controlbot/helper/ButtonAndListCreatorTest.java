package com.homeproject.controlbot.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ButtonAndListCreatorTest {

    private ButtonAndListCreator buttonAndListCreator = new ButtonAndListCreator();

    @Test
    void createListOfLists() {
        List<String> list = Arrays.asList("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten");
        List<List<String>> listOfLists = buttonAndListCreator.createListOfLists(list);
        Assertions.assertEquals(5, listOfLists.size());
        Assertions.assertEquals(2, listOfLists.get(0).size());
        Assertions.assertEquals(2, listOfLists.get(1).size());
        Assertions.assertEquals(2, listOfLists.get(4).size());
        Assertions.assertEquals("Two", listOfLists.get(0).get(1));
        Assertions.assertEquals("Four", listOfLists.get(1).get(1));
        Assertions.assertEquals("Five", listOfLists.get(2).get(0));
        Assertions.assertEquals("Seven", listOfLists.get(3).get(0));
        Assertions.assertEquals("Ten", listOfLists.get(4).get(1));

    }

    @Test
    void createKeyboardForRequest() {
        SendMessage sendMessage = new SendMessage();
        List<String> buttonNames = Arrays.asList("One", "Two", "Three", "Four", "Five");
        SendMessage sendMessageCheck = new SendMessage();
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow keyboardRowOne = new KeyboardRow();
        KeyboardRow keyboardRowTwo = new KeyboardRow();
        keyboardRowOne.add(new KeyboardButton("One"));
        keyboardRowOne.add(new KeyboardButton("Two"));
        keyboardRowOne.add(new KeyboardButton("Three"));
        keyboardRowTwo.add(new KeyboardButton("Four"));
        keyboardRowTwo.add(new KeyboardButton("Five"));
        List<KeyboardRow> keyboardRows = Arrays.asList(keyboardRowOne, keyboardRowTwo);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        sendMessageCheck.setReplyMarkup(replyKeyboardMarkup);
        buttonAndListCreator.createKeyboardForRequest(buttonNames, sendMessage);
        assertNull(sendMessage.getText());
        Assertions.assertEquals(sendMessageCheck.getReplyMarkup().toString(), sendMessage.getReplyMarkup().toString());
    }

    @Test
    void createButtonsInMessage() {
        SendMessage sendMessage = new SendMessage();
        List<List<String>> buttonNames = Arrays.asList(List.of("One", "Two"), List.of("Three"));
        buttonAndListCreator.createButtonsInMessage(sendMessage, buttonNames);

        SendMessage sendMessageCheck = new SendMessage();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButtonOne = new InlineKeyboardButton();
        inlineKeyboardButtonOne.setText("One");
        inlineKeyboardButtonOne.setCallbackData("One_BUTTON");
        InlineKeyboardButton inlineKeyboardButtonTwo = new InlineKeyboardButton();
        inlineKeyboardButtonTwo.setText("Two");
        inlineKeyboardButtonTwo.setCallbackData("Two_BUTTON");
        InlineKeyboardButton inlineKeyboardButtonThree = new InlineKeyboardButton();
        inlineKeyboardButtonThree.setText("Three");
        inlineKeyboardButtonThree.setCallbackData("Three_BUTTON");
        List<List<InlineKeyboardButton>> lists = Arrays.asList(
                Arrays.asList(inlineKeyboardButtonOne, inlineKeyboardButtonTwo),
                Arrays.asList(inlineKeyboardButtonThree));
        markup.setKeyboard(lists);
        sendMessageCheck.setReplyMarkup(markup);
        Assertions.assertEquals(sendMessageCheck.getReplyMarkup().toString(), sendMessage.getReplyMarkup().toString());
        assertNull(sendMessage.getText());
    }
}
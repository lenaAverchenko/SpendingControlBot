package com.homeproject.controlbot.helper;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class ButtonAndListCreator {
    public List<List<String>> createListOfLists(List<String> listOfAllStrings) {
        int counter = 0;
        List<List<String>> resultedListOfLists = new ArrayList<>();
        List<String> currentList = new ArrayList<>();
        for (String str : listOfAllStrings) {
            if (counter < 2) {
                currentList.add(str);
                counter++;
            } else {
                resultedListOfLists.add(currentList);
                currentList = new ArrayList<>();
                currentList.add(str);
                counter = 1;
            }
        }
        if (!currentList.isEmpty()) {
            resultedListOfLists.add(currentList);
        }
        return resultedListOfLists;
    }

    public void createKeyboardForRequest(List<String> buttonNames, SendMessage sendMessage) {
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

    public void createButtonsInMessage(SendMessage sendMessage, List<List<String>> buttonNames) {
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
}

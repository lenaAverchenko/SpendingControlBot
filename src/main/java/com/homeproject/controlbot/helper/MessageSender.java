package com.homeproject.controlbot.helper;

import com.homeproject.controlbot.repository.AutomatedMessageRepository;
import com.homeproject.controlbot.repository.BotUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSender {

    @Autowired
    private final BotUserRepository botUserRepository;
    @Autowired
    private AutomatedMessageRepository automatedMessageRepository;


    private  ButtonAndListCreator buttonAndListCreator = new ButtonAndListCreator();

    public SendMessage sendMessageWithKeyboard(long chatId, String textToSend, List<String> buttonNames) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        if (!buttonNames.isEmpty()) {
            buttonAndListCreator.createKeyboardForRequest(buttonNames, sendMessage);
        }
       return sendMessage;
    }

    public SendMessage sendMessageWithButtons(long chatId, String textToSend, List<List<String>> buttonNames) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        if (!buttonNames.isEmpty()) {
            buttonAndListCreator.createButtonsInMessage(sendMessage, buttonNames);
        }
        return sendMessage;
    }

    public List<SendMessage> checkLengthAndSendMes(long chatId, String textToSend) {
        char[] textToChars = textToSend.toCharArray();
        int lengthOfTheText = textToChars.length;
        List<SendMessage> listOfSendMessage = new ArrayList<>();
        if (lengthOfTheText < 4090) {
            listOfSendMessage.add(sendMessage(chatId, textToSend));
        } else {
            List<String> textsToSend = new ArrayList<>();
            StringBuilder temporaryString = new StringBuilder();
            int temporaryIndex = 0;
            while (lengthOfTheText > 4090 + temporaryIndex) {
                for (int i = temporaryIndex; i < 4090 + temporaryIndex; i++) {
                    if (i <= 4000 + temporaryIndex) {
                        temporaryString.append(textToChars[i]);
                    } else {
                        if (textToChars[i] == ' ') {
                            temporaryIndex = i;
                            break;
                        }
                        temporaryString.append(textToChars[i]);
                    }
                }
                textsToSend.add(temporaryString.toString());
                temporaryString = new StringBuilder();
            }
            if (temporaryIndex < lengthOfTheText) {
                for (int i = temporaryIndex; i < lengthOfTheText; i++) {
                    temporaryString.append(textToChars[i]);
                }
                textsToSend.add(temporaryString.toString());
            }
            for (String str : textsToSend) {
                listOfSendMessage.add(sendMessage(chatId, str));
            }
        }
        return listOfSendMessage;
    }

    public SendMessage sendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        return sendMessage;
    }

    public EditMessageText sendEditedMessage(long chatId, int messageId, String notificationMessage) {
        EditMessageText messageText = new EditMessageText();
        messageText.setChatId(String.valueOf(chatId));
        messageText.setText(notificationMessage);
        messageText.setMessageId((int) messageId);
        return messageText;
    }

}

package com.homeproject.controlbot.helper;

import com.homeproject.controlbot.repository.AutomatedMessageRepository;
import com.homeproject.controlbot.repository.BotUserRepository;
import javassist.expr.Instanceof;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class})
class MessageSenderTest {
    @InjectMocks
    private MessageSender messageSender;
    @Mock
    private BotUserRepository botUserRepository;
    @Mock
    private AutomatedMessageRepository automatedMessageRepository;
    private  ButtonAndListCreator buttonAndListCreator = new ButtonAndListCreator();


    @Test
    void sendMessageWithKeyboard() {
        SendMessage sendMessage = messageSender.sendMessageWithKeyboard(111L, "Test", List.of("One", "Two", "Three", "Four"));
        Assertions.assertEquals("111", sendMessage.getChatId());
        Assertions.assertEquals("Test", sendMessage.getText());
        Assertions.assertEquals(ReplyKeyboardMarkup.class, sendMessage.getReplyMarkup().getClass());
    }

    @Test
    void sendMessageWithButtons() {
        SendMessage sendMessage = messageSender.sendMessageWithButtons(123L, "Test text",
                List.of(
                        List.of("One", "Two"), List.of("Three", "Four"), List.of("Five", "Six")));
        Assertions.assertEquals("123", sendMessage.getChatId());
        Assertions.assertEquals("Test text", sendMessage.getText());
        Assertions.assertEquals(InlineKeyboardMarkup.class, sendMessage.getReplyMarkup().getClass());
    }

    @Test
    void checkLengthAndSendMesNotLong() {
        String textToSend = "Some text to check";
        List<SendMessage> list = messageSender.checkLengthAndSendMes(555L, textToSend);
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("555", list.get(0).getChatId());
        Assertions.assertEquals("Some text to check", list.get(0).getText());
        Assertions.assertEquals(18, list.get(0).getText().length());
    }
    @Test
    void checkLengthAndSendMesTooLong() {
        String text = "1".repeat(5000);
        List<SendMessage> list = messageSender.checkLengthAndSendMes(555L, text);
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals("555", list.get(0).getChatId());
        Assertions.assertEquals(4089, list.get(0).getText().length());
        Assertions.assertEquals(911, list.get(1).getText().length());
    }
    @Test
    void sendMessage() {
        SendMessage sendMessage = messageSender.sendMessage(888L, "Some text");
        Assertions.assertEquals("888", sendMessage.getChatId());
        Assertions.assertEquals("Some text", sendMessage.getText());
        assertNull(sendMessage.getReplyMarkup());
    }

    @Test
    void sendEditedMessage() {
        EditMessageText messageText = messageSender.sendEditedMessage(555L, 5, "Text to check");
        Assertions.assertEquals("555", messageText.getChatId());
        Assertions.assertEquals(5, messageText.getMessageId());
        Assertions.assertEquals("Text to check", messageText.getText());
    }
}
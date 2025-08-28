package org.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.LoginUrl;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class Bot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${keycloak.callback-url}")
    private String keycloakCallbackUrl;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message msg = update.getMessage();
            String text = msg.getText();
            if (text.startsWith("/start")) {
                String[] parts = text.split(" ");
                String startParam = parts.length > 1 ? parts[1] : null;

                if (startParam != null) {
                    String redirectUrl = keycloakCallbackUrl + "?t=" + startParam;

                    SendMessage response = new SendMessage();
                    response.setChatId(msg.getChatId().toString());
                    response.setText("Click the button below to complete your login:");

                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    InlineKeyboardButton button = new InlineKeyboardButton("Continue Login");
                    button.setLoginUrl(new LoginUrl(redirectUrl));
                    markup.setKeyboard(List.of(List.of(button)));

                    response.setReplyMarkup(markup);

                    try {
                        execute(response);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    sendMessage(msg.getChatId().toString(), "Welcome! This bot is used for login verification.");
                }
            }
        }
    }

    private void sendMessage(String chatId, String text) {
        try {
            execute(SendMessage.builder().chatId(chatId).text(text).build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

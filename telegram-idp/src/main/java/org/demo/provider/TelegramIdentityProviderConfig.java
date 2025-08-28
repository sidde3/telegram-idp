package org.demo.provider;

import org.demo.Constraint;
import org.keycloak.models.IdentityProviderModel;

import java.util.Map;

public class TelegramIdentityProviderConfig extends IdentityProviderModel {
    private static final String defaultTelegramUrl = "https://t.me/%s?start=%s";
    public TelegramIdentityProviderConfig(){}

    public TelegramIdentityProviderConfig(IdentityProviderModel model) {
        super(model);

    }

    public String getBotToken(){
        return getConfig().get(Constraint.BOT_TOKEN);
    }
    public void setBotToken(String botToken) {
        getConfig().put(Constraint.BOT_TOKEN, botToken);
    }
    public String getBotName(){
        return getConfig().get(Constraint.BOT_USER);
    }
    public void setBotUsername(String botName) {
        getConfig().put(Constraint.BOT_USER, botName);
    }
    public String getTelegramUrl(){
        return getConfig().getOrDefault(Constraint.TELEGRAM_URL,defaultTelegramUrl);
    }
    public void setTelegramApiUrl(String telegramUrl) {
        getConfig().put(Constraint.TELEGRAM_URL, telegramUrl);
    }
}

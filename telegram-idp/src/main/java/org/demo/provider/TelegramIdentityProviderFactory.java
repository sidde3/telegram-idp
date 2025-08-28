package org.demo.provider;

import lombok.extern.jbosslog.JBossLog;
import org.demo.Constraint;
import org.keycloak.Config;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JBossLog
public class TelegramIdentityProviderFactory extends AbstractIdentityProviderFactory<TelegramIdentityProvider> {
    @Override
    public String getName() {
        return Constraint.NAME;
    }

    @Override
    public TelegramIdentityProvider create(KeycloakSession keycloakSession, IdentityProviderModel model) {
        return new TelegramIdentityProvider(keycloakSession, new TelegramIdentityProviderConfig(model));
    }

    @Override
    public IdentityProviderModel createConfig() {
        log.infof("Configuration: %s", super.getConfigProperties());
        return new TelegramIdentityProviderConfig();
    }

    @Override
    public String getId() {
        return Constraint.PROVIDER_ID;
    }
    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> configProperties = super.getConfigProperties();

        List<ProviderConfigProperty> filtered = configProperties.stream()
                .filter(prop -> !List.of("clientId", "clientSecret").contains(prop.getName()))
                .collect(Collectors.toList());

        //List<ProviderConfigProperty> configProperties = new ArrayList<>();
        ProviderConfigProperty botToken = new ProviderConfigProperty();
        botToken.setName(Constraint.BOT_TOKEN);
        botToken.setLabel("Bot Token");
        botToken.setHelpText("Telegram bot token provided by BotFather");
        botToken.setType(ProviderConfigProperty.PASSWORD);
        configProperties.add(botToken);

        ProviderConfigProperty botName = new ProviderConfigProperty();
        botName.setName(Constraint.BOT_USER);
        botName.setLabel("Bot Username");
        botName.setHelpText("Telegram bot's username without @");
        botName.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(botName);

        ProviderConfigProperty telegramUrl = new ProviderConfigProperty();
        telegramUrl.setName(Constraint.TELEGRAM_URL);
        telegramUrl.setLabel("Telegram API URL");
        telegramUrl.setHelpText("Base URL for Telegram API (default: https://t.me/%s?start=%s)");
        telegramUrl.setDefaultValue("https://t.me/%s?start=%s");
        telegramUrl.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(telegramUrl);

        return configProperties;
    }
}

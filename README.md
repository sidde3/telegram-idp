# 🤖 Keycloak Telegram Identity Provider (SPI)

This project enables **Telegram to act as an Identity Provider (IDP)** for **Keycloak**, allowing users to authenticate via the **Telegram login button** through a bot.

The implementation is built as a **Maven multi-module project** and consists of two components:

---

## 📁 Project Structure
```text

telegram-auth-parent/
│
├── telegram-idp/ # Keycloak Identity Provider SPI for Telegram
└── telegram-bot/ # Telegram Bot that handles login interactions

```
---

## 🚀 Overview

### 1. `telegram-idp` — Keycloak SPI

This module provides a custom **Identity Provider SPI** for Keycloak that delegates authentication to **Telegram**.

- Requires a Telegram **Bot Token**
- Implements the **Telegram Login Widget**
- Compatible with **Keycloak 26**
- Developed with **JDK 17**

> 🔐 **Note:** While configuring the Telegram IDP in Keycloak, you will be prompted for a `Client ID` and `Client Secret`.  
> These are **not required** and can be **left blank or ignored**.

---

### 2. `telegram-bot` — Telegram Login Bot

This module interacts with Telegram's Bot API to:

- Send users a **Telegram login button**
- Receive and validate Telegram login data
- Redirect the user back to Keycloak to complete authentication

Configuration is handled via `application.properties`.

---

## ⚙️ Setup & Configuration

### ✅ Prerequisites

- Java 17
- Maven 3.x
- Keycloak 26
- A registered Telegram bot with a valid **Bot Token**
- Access to a hosted `telegram-bot` service endpoint (can be localhost during development)

---

### 🧩 1. Configure Telegram Bot

- Create a bot using [@BotFather](https://t.me/BotFather) on Telegram
- Obtain the **Bot Token**
- Choose a **Bot Name**

---

### 🛠️ 2. Deploy `telegram-idp` to Keycloak

1. Build the project:

   ```bash
   mvn clean install
2. Copy the JAR to your Keycloak providers directory:
    ```bash
   cp telegram-idp/target/telegram-idp.jar $KEYCLOAK_HOME/providers/
    ```
3. Restart Keycloak:
    ```bash
   ks.sh start

### ⚙️ 3. Configure telegram-bot

In telegram-bot/src/main/resources/application.properties:
```text
telegram.bot.token=YOUR_TELEGRAM_BOT_TOKEN
telegram.bot.username=YOUR_TELEGRAM_BOT_NAME
telegram.redirect.url=https://your-keycloak-host/auth/realms/{realm}/broker/telegram/endpoint
```
### ▶️ 4. Run the Telegram Bot

You can run the bot as a Spring Boot app:
```text
cd telegram-bot
mvn spring-boot:run
```

## 📌 How It Works
1. User clicks "Login with Telegram" on your app or site
2. Telegram bot sends a login button
3. User authenticates via Telegram
4. Bot forwards login payload to Keycloak broker endpoint
5. Keycloak completes the login session
package ru.kuzmenko.bot;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kuzmenko.exception.ServiceException;
import ru.kuzmenko.service.ExchangeRatesService;
import ru.kuzmenko.utils.Currency;

import javax.xml.xpath.XPathExpressionException;
import java.time.LocalDate;

@Component
public class ExchangeRatesBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeRatesBot.class);

    @Autowired
    private ExchangeRatesService exchangeRatesService;

    private static final String START = "/start";

    public ExchangeRatesBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    private void startCommand(Long chatId, String userName) {
        var text = """
                Welcome to the Daily Foreign Exchange Rates for U.S. Dollar (USD), %s!
                                
                Here you can find out the USD exchange rate to the other currencies.
                                
                Choose necessary currency:
                EUR, GBP, JPY, AUD, CHF, CAD, DKK, LKR, TND, VND, TMT, PKR, BGN, RUB, GEL, SAR, PLN, KZT, COP,
                BHD, EGP, KRW, DZD, PAB, HKD, MAD, ZAR, IQD, BOB, KWD, THB, TWD, UZS, OMR, ILS, PEN, TJS, SEK,
                SGD, HUF, UAH, CLP, DOP, CNY, ISK, AZN, HTG, ANG, LBP, MYR, IRR, UYU, JOD, PHP, XOF, LYD, NZD,
                TRY, NGN, RSD, NOK, QAR, CZK, BYN, ARS, VES, BDT, RON, MDL, CRC, AED, IDR, MXN, AMD, PYG, BRL,
                INR, NPR, XAF, KGS.
                """;
        var formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    private void sendMessage(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error("Error sending message", e);
        }
    }

    private void currencyCommand(Long chatId, String message) throws XPathExpressionException {
        String formattedText;
        try {
            var currency = exchangeRatesService.getExchangeRate(message);
            var text = "Current exchange rate at %s is %s.";
            formattedText = String.format(text, LocalDate.now(), currency);
        } catch (ServiceException e) {
            LOG.error("Error getting currency exchange rate", e);
            formattedText = "Can't get currency exchange rate now. Try again later.";
        }
        sendMessage(chatId, formattedText);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        var chatId = update.getMessage().getChatId();
        var message = update.getMessage().getText();
        if (START.equals(message)) {
            String userName = update.getMessage().getChat().getFirstName();
            startCommand(chatId, userName);
            return;
        }
        if (EnumUtils.isValidEnum(Currency.class, message)) {
            try {
                currencyCommand(chatId, message);
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
            }
        } else {
            sendMessage(chatId, "Not valid currency.\n" +
                    "Choose necessary currency:\n" +
                    "EUR, GBP, JPY, AUD, CHF, CAD, DKK, LKR, TND, VND, TMT, PKR, BGN, RUB, GEL, SAR, PLN, KZT, COP,\n" +
                    "BHD, EGP, KRW, DZD, PAB, HKD, MAD, ZAR, IQD, BOB, KWD, THB, TWD, UZS, OMR, ILS, PEN, TJS, SEK,\n" +
                    "SGD, HUF, UAH, CLP, DOP, CNY, ISK, AZN, HTG, ANG, LBP, MYR, IRR, UYU, JOD, PHP, XOF, LYD, NZD,\n" +
                    "TRY, NGN, RSD, NOK, QAR, CZK, BYN, ARS, VES, BDT, RON, MDL, CRC, AED, IDR, MXN, AMD, PYG, BRL,\n" +
                    "INR, NPR, XAF, KGS.");
        }
    }

    @Override
    public String getBotUsername() {
        return "kuzmenko_exchange_rates_bot";
    }
}

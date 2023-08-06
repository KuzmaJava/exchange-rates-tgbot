package ru.kuzmenko.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.kuzmenko.exception.ServiceException;

import java.io.IOException;
import java.util.Optional;

@Component
public class ExchangeRatesClient {

    @Autowired
    private OkHttpClient client;

    @Value("${floatrates.currency.rates.xml.url}")
    private String floatRatesCurrencyRatesXmlUrl;

    public Optional<String> getCurrencyRatesXML() throws ServiceException {
        var request = new Request.Builder()
                .url(floatRatesCurrencyRatesXmlUrl)
                .build();

        try (var response = client.newCall(request).execute()) {
            var body = response.body();
            return body == null ? Optional.empty() : Optional.of(body.string());
        } catch (IOException e) {
            throw new ServiceException("Error getting exchange rates", e);
        }
    }
}

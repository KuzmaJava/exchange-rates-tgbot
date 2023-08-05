package ru.kuzmenko.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import ru.kuzmenko.client.ExchangeRatesClient;
import ru.kuzmenko.exception.ServiceException;
import ru.kuzmenko.service.ExchangeRatesService;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

@Service
public class ExchangeRateServiceImpl implements ExchangeRatesService {

    String CURRENCY_XPATH = "/channel/item[targetCurrency='?']/title";

    @Autowired
    private ExchangeRatesClient client;

    @Override
    public String getExchangeRate(String currency) throws ServiceException, XPathExpressionException {
        var xmlOptional = client.getCurrencyRatesXML();
        String xml = xmlOptional.orElseThrow(
                () -> new ServiceException("Не удалось получить XML")
        );
        return extractCurrencyValueFromXML(xml, CURRENCY_XPATH.replace("?", currency));
    }

    private String extractCurrencyValueFromXML(String xml, String xpathExpression) throws XPathExpressionException {
        var source = new InputSource(new StringReader(xml));
        try {
            var xpath = XPathFactory.newInstance().newXPath();
            var document = (Document) xpath.evaluate("/", source, XPathConstants.NODE);

            return xpath.evaluate(xpathExpression, document);
        } catch (XPathExpressionException e) {
            throw new XPathExpressionException(e.getMessage());
        }
    }
}

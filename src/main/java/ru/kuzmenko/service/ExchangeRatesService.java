package ru.kuzmenko.service;

import ru.kuzmenko.exception.ServiceException;

import javax.xml.xpath.XPathExpressionException;

public interface ExchangeRatesService {

    String getExchangeRate(String currency) throws ServiceException, XPathExpressionException;

    void clearCurrencyCache();
}

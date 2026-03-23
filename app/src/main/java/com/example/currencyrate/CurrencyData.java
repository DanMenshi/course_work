package com.example.currencyrate;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CurrencyData {
    private static final String CBR_URL = "https://www.cbr.ru/scripts/XML_daily.asp";
    private static List<Currency> currencies = Collections.synchronizedList(new ArrayList<>());
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    static {
        // Initial defaults
        currencies.add(new Currency("RUB", "Российский рубль", 1.0, "₽"));
        currencies.add(new Currency("USD", "Доллар США", 93.42, "$"));
        currencies.add(new Currency("EUR", "Евро", 102.25, "€"));
        currencies.add(new Currency("CNY", "Юань", 12.77, "¥"));
        currencies.add(new Currency("KZT", "Тенге", 0.19, "₸"));
    }

    public interface UpdateCallback {
        void onUpdate();
    }

    public static List<Currency> getCurrencies() {
        return new ArrayList<>(currencies);
    }

    public static Currency getByCode(String code) {
        synchronized (currencies) {
            for (Currency c : currencies) {
                if (c.getCode().equals(code)) return c;
            }
        }
        return currencies.get(0); // RUB
    }

    public static void fetchRates(UpdateCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(CBR_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();

                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(inputStream, null);

                String currentCode = null;
                double currentValue = 0;
                int currentNominal = 1;
                String currentName = null;

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = parser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if ("CharCode".equals(tagName)) {
                                currentCode = parser.nextText();
                            } else if ("Nominal".equals(tagName)) {
                                currentNominal = Integer.parseInt(parser.nextText());
                            } else if ("Name".equals(tagName)) {
                                currentName = parser.nextText();
                            } else if ("Value".equals(tagName)) {
                                String valStr = parser.nextText().replace(",", ".");
                                currentValue = Double.parseDouble(valStr);
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if ("Valute".equals(tagName) && currentCode != null) {
                                double ratePerUnit = currentValue / currentNominal;
                                updateOrAddCurrency(currentCode, currentName, ratePerUnit);
                            }
                            break;
                    }
                    eventType = parser.next();
                }
                inputStream.close();
                mainHandler.post(callback::onUpdate);

            } catch (Exception e) {
                Log.e("CurrencyData", "Error fetching rates", e);
            }
        });
    }

    private static void updateOrAddCurrency(String code, String name, double rate) {
        synchronized (currencies) {
            boolean found = false;
            for (Currency c : currencies) {
                if (c.getCode().equals(code)) {
                    c.setRate(rate);
                    found = true;
                    break;
                }
            }
            if (!found) {
                String symbol = getSymbolForCode(code);
                currencies.add(new Currency(code, name, rate, symbol));
            }
        }
    }

    private static String getSymbolForCode(String code) {
        switch (code) {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "JPY": return "¥";
            case "CNY": return "¥";
            case "KZT": return "₸";
            case "TRY": return "₺";
            default: return "";
        }
    }
}

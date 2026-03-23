package com.example.currencyrate;

public class Currency {
    private String code;
    private String name;
    private double rate; // Rate relative to RUB
    private String symbol;

    public Currency(String code, String name, double rate, String symbol) {
        this.code = code;
        this.name = name;
        this.rate = rate;
        this.symbol = symbol;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public double getRate() { return rate; }
    public String getSymbol() { return symbol; }
    
    public void setRate(double rate) { this.rate = rate; }
}

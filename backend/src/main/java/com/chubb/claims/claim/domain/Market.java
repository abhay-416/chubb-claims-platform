package com.chubb.claims.claim.domain;

public enum Market {
    SINGAPORE("SGD", "S$"),
    HONG_KONG("HKD", "HK$"),
    AUSTRALIA("AUD", "A$"),
    THAILAND("THB", "฿"),
    MALAYSIA("MYR", "RM"),
    INDONESIA("IDR", "Rp");

    private final String defaultCurrency;
    private final String currencySymbol;

    Market(String defaultCurrency, String currencySymbol) {
        this.defaultCurrency = defaultCurrency;
        this.currencySymbol = currencySymbol;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }
}

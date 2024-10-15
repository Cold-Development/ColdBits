package dev.padrewin.coldbits.conversion;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.coldbits.ColdBits;
import dev.padrewin.coldbits.conversion.converter.GamePointsConverter;
import dev.padrewin.coldbits.conversion.converter.TokenManagerConverter;

public enum CurrencyPlugin {

    TokenManager(TokenManagerConverter.class),
    GamePoints(GamePointsConverter.class);

    private final Class<? extends CurrencyConverter> converterClass;

    CurrencyPlugin(Class<? extends CurrencyConverter> converterClass) {
        this.converterClass = converterClass;
    }

    public CurrencyConverter getConverter() {
        try {
            return this.converterClass.getConstructor(ColdPlugin.class).newInstance(ColdBits.getInstance());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static CurrencyPlugin get(String name) {
        for (CurrencyPlugin currencyPlugin : values())
            if (currencyPlugin.name().equalsIgnoreCase(name))
                return currencyPlugin;
        return null;
    }

}

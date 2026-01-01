package org.fesg.i18n;

public enum AppLanguage {
    PL("pl", "PL"),
    EN("en", "GB");

    private final String languageCode;
    private final String countryCode;

    AppLanguage(String languageCode, String countryCode) {
        this.languageCode = languageCode;
        this.countryCode = countryCode;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public static AppLanguage fromCode(String code) {
        if (code == null || code.isBlank()) {
            return PL;
        }
        for (AppLanguage lang : values()) {
            if (lang.languageCode.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        return PL;
    }
}
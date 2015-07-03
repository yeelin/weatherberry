package com.example.yeelin.homework.weatherberry.loader;


/**
 * Created by ninjakiki on 4/10/15.
 */

public enum LoaderIds {
    CURRENT_WEATHER_LOADER(100),
    DAILY_FORECAST_LOADER(200),
    TRIHOUR_FORECAST_LOADER(300),
    FAVORITES_LOADER(400);

    private int value;
    LoaderIds(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static LoaderIds getLoaderIdForInt(int i) {
        for (LoaderIds idType: values()) {
            if (idType.getValue() == i) {
                return idType;
            }
        }
        return null;
    }

}

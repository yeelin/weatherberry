package com.example.yeelin.homework.weatherberry.fragmentUtils;

import android.support.annotation.Nullable;

/**
 * Created by ninjakiki on 4/24/15.
 */
public final class FormatUtils {
    /**
     * Helper method to capitalize the first letter of the description
     * @param description
     * @return
     */
    public static String formatDescription(@Nullable String description) {
        if (description != null && description.length() >= 2) {
            return Character.toUpperCase(description.charAt(0)) + description.substring(1);
        }
        return "";
    }
}

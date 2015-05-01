package com.example.yeelin.homework2.h312yeelin.fragmentUtils;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Created by ninjakiki on 4/24/15.
 */
public final class AnimationUtils {

    public static void crossFadeViews (final Context context, final View fadeInView, final View fadeOutView) {
        long shortDuration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

        //fadeInView should initially be transparent but visible
        fadeInView.setAlpha(0f);
        fadeInView.setVisibility(View.VISIBLE);

        //animate fadeInView from 0f to 1f
        ViewCompat.animate(fadeInView)
                .alpha(1f)
                .setDuration(shortDuration)
                .withLayer();

        //animate fadeOutView from 1f to 0f
        ViewCompat.animate(fadeOutView)
                .alpha(0f)
                .withLayer()
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        fadeOutView.setVisibility(View.GONE);
                    }
                });
    }
}

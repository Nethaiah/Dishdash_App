package com.example.dishdash;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InsetsUtil {
    public static void applyInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    v.getPaddingLeft(),
                    systemBarsInsets.top,
                    v.getPaddingRight(),
                    systemBarsInsets.bottom
            );
            return WindowInsetsCompat.CONSUMED;
        });
    }
}

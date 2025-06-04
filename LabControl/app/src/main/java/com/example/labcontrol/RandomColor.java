package com.example.labcontrol;

import android.graphics.Color;
import java.util.Random;

public class RandomColor {
    public static int getRandomColor() {
        Random random = new Random();
        int r, g, b;
        // only bright colors
        do {
            r = random.nextInt(256);
            g = random.nextInt(256);
            b = random.nextInt(256);
        } while ((r * 0.299 + g * 0.587 + b * 0.114) < 186);
        return Color.rgb(r, g, b);
    }
}

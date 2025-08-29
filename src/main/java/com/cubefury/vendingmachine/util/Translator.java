package com.cubefury.vendingmachine.util;

import net.minecraft.client.resources.I18n;

import com.cubefury.vendingmachine.VendingMachine;

public class Translator {

    public static String translate(String text, Object... args) {
        String out = I18n.format(text, args);
        if (out.startsWith("Format error: ")) {
            return text; // TODO: Find a more reliable way of detecting translation failure
        }
        return out;
    }

    public static int getColor(String key) {
        String hex = translate(key);
        int color = 0x000000;
        if (hex.length() <= 6) {
            try {
                color = Integer.parseUnsignedInt(hex, 16);
            } catch (NumberFormatException e) {
                VendingMachine.LOG.warn("Couldn't format color correctly for: " + key, e);
            }
        }
        return color;
    }
}

package com.coinomi.core.util;

public class CryptoWaterUtils {
    public static boolean isLanaCoin(String address) {
        return address.toLowerCase().startsWith("l");
    }

    public static boolean isTajCoin(String address) {
        return address.toLowerCase().startsWith("t");
    }

    public static boolean isARCOCoin(String address) {
        return address.toLowerCase().startsWith("a");
    }

}

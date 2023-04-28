package com.example.catfisharea.ultilities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class DecimalHelper {

    public static String formatText(Object text) {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
        dfs.setDecimalSeparator('.');
        dfs.setGroupingSeparator(',');
        dfs.setCurrencySymbol("VNĐ");

        DecimalFormat df = new DecimalFormat("#,###.##", dfs);
        return df.format(text);
    }

    public static Number parseText(String text) throws ParseException {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
        dfs.setDecimalSeparator('.');
        dfs.setGroupingSeparator(',');
        dfs.setCurrencySymbol("VNĐ");

        DecimalFormat df = new DecimalFormat("#,###.##", dfs);
        if (text.isEmpty()) return 0;
        return df.parse(text);
    }
}

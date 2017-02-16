package com.orcchg.vikstra.domain.util;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import timber.log.Timber;

public class StringUtility {

    public static String encodeLink(String link, String enc) {
        link = link.toLowerCase();
        if (isAsciiPrintable(link)) return link;

        int index = link.indexOf("http");
        String prefix = "";
        if (index >= 0) {
            link = link.substring(index + 4);
            prefix = "http";
        }
        try {
            String output = prefix + URLEncoder.encode(link, enc);
            Timber.v("Output link: %s", output);
            return output;
        } catch (UnsupportedEncodingException e) {
            Timber.w(e, "Link wasn't encoded properly!");
        }
        return link;
    }

    private static boolean isAsciiPrintable(String str) {
        if (TextUtils.isEmpty(str)) return false;

        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (!isAsciiPrintable(str.charAt(i))) return false;
        }
        return true;
    }

    private static boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }
}

package com.orcchg.vikstra.app.ui.util;

public class ContextUtility {
    // TODO: move to shared prefs

    private static int sIncrementalValue = 1;

    public static String defaultTitle() {
        return "Default " + sIncrementalValue++;
    }
}

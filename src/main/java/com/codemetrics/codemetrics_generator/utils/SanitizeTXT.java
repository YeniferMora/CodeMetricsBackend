package com.codemetrics.codemetrics_generator.utils;

public class SanitizeTXT {
    public static String escapeSpecialCharacters(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\'", "\\\'")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}


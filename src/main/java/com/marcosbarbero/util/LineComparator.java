package com.marcosbarbero.util;

import java.util.Comparator;

/**
 * @author Marcos Barbero
 */
public class LineComparator implements Comparator<String> {

    private static final String REGEX = "//s+";

    @Override
    public int compare(String line1, String line2) {
        line1 = removeWhitespaces(line1);
        line2 = removeWhitespaces(line2);
        return String.CASE_INSENSITIVE_ORDER.compare(line1, line2);
    }

    private String removeWhitespaces(String line) {
        return line.replaceAll(REGEX, "");
    }
}

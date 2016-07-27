package com.marcosbarbero;

import com.marcosbarbero.io.ExternalSort;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * Main class to perform a solution for Schibsted use case of large-files reading and sorting.
 *
 * @author Marcos Barbero
 */
public class Solution {

    private static final String INPUT = "input.txt";
    private static final String OUTPUT = "output.txt";

    private static final String PROPERTY_INPUT = "input.file";
    private static final String PROPERTY_OUTPUT = "output.file";

    public static void main(String... args) throws IOException {
        String value = getValue(PROPERTY_INPUT);
        final String input = value != null ? value : INPUT;
        value = getValue(PROPERTY_OUTPUT);
        final String output = value != null ? value : OUTPUT;
        System.out.println("Starting the external sorting reading '" + input + "' file...");
        Instant start = Instant.now();
        final Comparator<String> caseInsensitiveOrder = String.CASE_INSENSITIVE_ORDER;
        System.out.println("External sort in batch...");
        final List<File> files = ExternalSort.sortInBatch(new File(input), caseInsensitiveOrder);
        System.out.println("Merging files...");
        ExternalSort.mergeSortedFiles(files, new File(output), caseInsensitiveOrder);
        System.out.println("The sorting is finished! You can verify the result on file '" + output + "'");
        System.out.println("This program took '" + Duration.between(start, Instant.now()).get(ChronoUnit.SECONDS) + "' seconds to run.");
    }

    /**
     * Returns the property value.
     *
     * @param property The argument
     * @return The property value
     */
    private static String getValue(String property) {
        return System.getProperty(property);
    }

}

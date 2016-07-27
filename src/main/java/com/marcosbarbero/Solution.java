package com.marcosbarbero;

import com.marcosbarbero.io.ExternalSort;
import com.marcosbarbero.io.LineComparator;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Main class to perform a solution for Schibsted use case of large-files reading and sorting.
 *
 * @author Marcos Barbero
 */
public class Solution {

    private static final Logger logger = Logger.getLogger(Solution.class.getCanonicalName());

    private static final String INPUT = "input.txt";
    private static final String OUTPUT = "output.txt";

    public static void main(String... args) throws IOException {
        Instant start = Instant.now();
        final Comparator<String> lineComparator = new LineComparator();
        final List<File> files = ExternalSort.sortInBatch(new File(INPUT), lineComparator);
        ExternalSort.mergeSortedFiles(files, new File(OUTPUT), lineComparator);
        logger.info("Duration in seconds: " + Duration.between(start, Instant.now()).get(ChronoUnit.SECONDS));
    }

}

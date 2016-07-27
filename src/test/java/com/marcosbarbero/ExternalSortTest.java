package com.marcosbarbero;

import com.marcosbarbero.io.ExternalSort;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Marcos Barbero
 */
public class ExternalSortTest {

    private static final String INPUT_FILE = "sample-input.txt";
    private static final Comparator<String> COMPARATOR = String.CASE_INSENSITIVE_ORDER;

    private static final String[] EXPECTED_HEADER_RESULTS = {"Marcos Barbero", "Marcos Gomes", "Marcos Henrique", "Marcos Henrique Gomes Barbero"};

    private File inputFile;
    private File outputFile;

    @Before
    public void setUp() throws IOException {
        inputFile = new File(ExternalSortTest.class.getClassLoader().getResource(INPUT_FILE).getFile());
        outputFile = File.createTempFile("outputFile", "fatFile");
    }

    @Test
    public void testSortInBatch() throws Exception {
        List<File> listOfFiles = ExternalSort.sortInBatch(inputFile, COMPARATOR);
        assertEquals(1, listOfFiles.size());
    }

    @Test
    public void testMergeSortedFiles() throws Exception {
        List<File> listOfFiles = ExternalSort.sortInBatch(inputFile, COMPARATOR);
        ExternalSort.mergeSortedFiles(listOfFiles, outputFile, COMPARATOR);

        BufferedReader reader = new BufferedReader(new FileReader(outputFile));
        List<String> result = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            result.add(line);
        }
        reader.close();

        assertArrayEquals(Arrays.toString(result.toArray()), EXPECTED_HEADER_RESULTS, result.toArray());
    }
}

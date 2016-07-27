package com.marcosbarbero.io;

import com.marcosbarbero.util.Utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

/**
 * @author Marcos Barbero
 */
public class ExternalSort {

    private static final int DEFAULT_MAX_TEMP_FILES = 1024;
    private static final String TEMP_FILE_PREFIX = "sortInBatch";
    private static final String TEMP_FILE_SUFFIX = "flatFile";
    private static final String EMPTY_STRING = "";

    /**
     * Sort the file in batch, extracting int into n tmp files.
     *
     * @param file       The file to be sorted
     * @param comparator The comparator to be used in whole process
     * @return A list of sorted temp files
     * @throws IOException
     */
    public static List<File> sortInBatch(final File file, final Comparator<String> comparator) throws IOException {
        final Charset charset = Charset.defaultCharset();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
        return sortInBatch(reader, file.length(), comparator, DEFAULT_MAX_TEMP_FILES, Utils.estimateAvailableMemory(), charset);
    }

    /**
     * Sort the file in batch, extracting int into n tmp files.
     *
     * @param reader       The buffered read to be sorted
     * @param dataLength   The length of current file
     * @param comparator   The comparator
     * @param maxTempFiles The max temp files to be created
     * @param maxMemory    The available memory
     * @param charset      The Charset
     * @return A list of sorted temp files
     * @throws IOException
     */
    private static List<File> sortInBatch(final BufferedReader reader, final long dataLength,
                                          final Comparator<String> comparator, final int maxTempFiles,
                                          final long maxMemory, final Charset charset) throws IOException {
        List<File> files = new ArrayList<>();
        long blockSize = Utils.estimateBestSizeOfBlocks(dataLength, maxTempFiles, maxMemory);
        try {
            List<String> lines = new ArrayList<>();
            String line = EMPTY_STRING;
            try {
                while (line != null) {
                    long currentBlockSize = 0;
                    while ((currentBlockSize < blockSize) && ((line = reader.readLine()) != null)) {
                        lines.add(line);
                        currentBlockSize += Utils.estimatedSizeOf(line);
                    }
                    add(files, lines, comparator, charset);
                    lines.clear();
                }
            } catch (EOFException e) {
                if (lines.size() > 0) {
                    add(files, lines, comparator, charset);
                    lines.clear();
                }
            }
        } finally {
            reader.close();
        }
        return files;
    }

    /**
     * Add the sorted file into a list of temp files.
     *
     * @param files      The list of temp files
     * @param lines      The lines of the current file
     * @param comparator The comparator
     * @param charset    The charset
     * @throws IOException
     */
    private static void add(final List<File> files, final List<String> lines,
                            final Comparator<String> comparator, final Charset charset) throws IOException {
        files.add(sortAndSave(lines, comparator, charset));
    }


    /**
     * Merge the sorted files.
     *
     * @param files      The files
     * @param outputFile The output file
     * @param comparator The comparator
     * @return The number of files merged
     * @throws IOException
     */
    public static int mergeSortedFiles(final List<File> files, final File outputFile, final Comparator<String> comparator)
            throws IOException {
        return mergeSortedFiles(files, outputFile, comparator, Charset.defaultCharset());
    }

    /**
     * Merge the sorted files
     *
     * @param files      The files to be merged
     * @param outputFile The output file
     * @param comparator The comparator
     * @param charset    The Charset
     * @return The number of files merged
     * @throws IOException
     */
    private static int mergeSortedFiles(final List<File> files, final File outputFile,
                                       final Comparator<String> comparator, final Charset charset) throws IOException {
        final ArrayList<BinaryFileBuffer> buffers = new ArrayList<>();
        for (File file : files) {
            final InputStream in = new FileInputStream(file);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
            buffers.add(new BinaryFileBuffer(reader));
            file.delete();
        }
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile, false), charset));
        return mergeSortedFiles(writer, comparator, buffers);
    }

    /**
     * Merge the sorted files.
     *
     * @param writer     BufferedWriter
     * @param comparator The comparator to be used
     * @param buffers    A list of BinaryFileBuffer
     * @return The number of files merged
     * @throws IOException
     */
    private static int mergeSortedFiles(final BufferedWriter writer,
                                        final Comparator<String> comparator,
                                        final List<BinaryFileBuffer> buffers) throws IOException {

        final PriorityQueue<BinaryFileBuffer> queue = new PriorityQueue<>(
                buffers.size(), binaryFileBufferComparator(comparator)
        );

        queue.addAll(buffers.stream().filter(buffer -> !buffer.empty()).collect(Collectors.toList()));

        int counter = 0;
        try {
            while (queue.size() > 0) {
                final BinaryFileBuffer buffer = queue.poll();
                final String line = buffer.pop();
                writer.write(line);
                writer.newLine();
                ++counter;
                if (buffer.empty()) {
                    buffer.close();
                } else {
                    queue.add(buffer);
                }
            }
        } finally {
            writer.close();
            for (BinaryFileBuffer buffer : queue) {
                buffer.close();
            }
        }
        return counter;

    }

    /**
     * Creates a Comparator for BinaryFileBuffer.
     *
     * @param comparator The default comparator used on whole process
     * @return The BinaryFileBuffer comparator
     */
    private static Comparator<BinaryFileBuffer> binaryFileBufferComparator(final Comparator<String> comparator) {
        return (i, j) -> comparator.compare(i.peek(), j.peek());
    }

    /**
     * Sort the lines and save into a temporary file.
     *
     * @param lines      The text lines
     * @param comparator The comparator
     * @param charset    The Charset
     * @return The temp file
     * @throws IOException
     */
    private static File sortAndSave(final List<String> lines,
                                    final Comparator<String> comparator,
                                    final Charset charset) throws IOException {
        final List<String> tempLines = lines.parallelStream().sorted(comparator).collect(Collectors.toCollection(ArrayList<String>::new));
        final File tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
        tempFile.deleteOnExit();
        final OutputStream out = new FileOutputStream(tempFile);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, charset))) {
            for (String line : tempLines) {
                writer.write(line);
                writer.newLine();
            }
        }

        return tempFile;
    }
}
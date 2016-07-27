package com.marcosbarbero.io;

import com.marcosbarbero.util.Utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Marcos Barbero
 */
public class ExternalSort {

    private static final int DEFAULT_MAX_TEMP_FILES = 1024;
    private static final String TEMP_FILE_PREFIX = "sortInBatch";
    private static final String TEMP_FILE_SUFFIX = "flatFile";
    private static final String EMPTY_STRING = "";

    public static List<File> sortInBatch(final File file, final Comparator<String> comparator) throws IOException {
        final Charset charset = Charset.defaultCharset();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
        return sortInBatch(reader, file.length(), comparator, DEFAULT_MAX_TEMP_FILES, Utils.estimateAvailableMemory(), charset);
    }

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

    private static void add(final List<File> files, final List<String> lines,
                            final Comparator<String> comparator, final Charset charset) throws IOException {
        files.add(sortAndSave(lines, comparator, charset));
    }


    public static int mergeSortedFiles(final List<File> files, final File outputFile, final Comparator<String> comparator)
            throws IOException {
        return mergeSortedFiles(files, outputFile, comparator, Charset.defaultCharset());
    }

    public static int mergeSortedFiles(final List<File> files, final File outputFile,
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

    private static Comparator<BinaryFileBuffer> binaryFileBufferComparator(final Comparator<String> comparator) {
        return (i, j) -> comparator.compare(i.peek(), j.peek());
    }

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
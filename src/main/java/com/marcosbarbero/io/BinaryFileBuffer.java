package com.marcosbarbero.io;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * This is essentially a thin wrapper on top of a BufferedReader... which keeps
 * the last line in memory.
 */
final class BinaryFileBuffer {

    private BufferedReader reader;
    private String cache;

    public BinaryFileBuffer(final BufferedReader reader) throws IOException {
        this.reader = reader;
        reload();
    }

    public void close() throws IOException {
        this.reader.close();
    }

    public boolean empty() {
        return this.cache == null;
    }

    public String peek() {
        return this.cache;
    }

    public String pop() throws IOException {
        String answer = peek();
        reload();
        return answer;
    }

    private void reload() throws IOException {
        this.cache = this.reader.readLine();
    }
}

package com.marcosbarbero.util;

/**
 * @author Marcos Barbero
 */
public class Utils {

    private static int OBJ_OVERHEAD;

    /**
     * Class initializations.
     */
    static {
        // By default we assume 64 bit JVM
        // (defensive approach since we will get
        // larger estimations in case we are not sure)
        boolean IS_64_BIT_JVM = true;
        // check the system property "sun.arch.data.model"
        // not very safe, as it might not work for all JVM implementations
        // nevertheless the worst thing that might happen is that the JVM is 32bit
        // but we assume its 64bit, so we will be counting a few extra bytes per string object
        // no harm done here since this is just an approximation.
        String arch = System.getProperty("sun.arch.data.model");
        if (arch != null) {
            if (arch.contains("32")) {
                // If exists and is 32 bit then we assume a 32bit JVM
                IS_64_BIT_JVM = false;
            }
        }
        // The sizes below are a bit rough as we don't take into account
        // advanced JVM options such as compressed oops
        // however if our calculation is not accurate it'll be a bit over
        // so there is no danger of an out of memory error because of this.
        int OBJ_HEADER = IS_64_BIT_JVM ? 16 : 8;
        int ARR_HEADER = IS_64_BIT_JVM ? 24 : 12;
        int OBJ_REF = IS_64_BIT_JVM ? 8 : 4;
        int INT_FIELDS = 12;
        OBJ_OVERHEAD = OBJ_HEADER + INT_FIELDS + OBJ_REF + ARR_HEADER;

    }

    /**
     * Estimates the size of a {@link String} object in bytes.
     *
     * @param s The string to estimate memory footprint.
     * @return The <strong>estimated</strong> size in bytes.
     */
    public static long estimatedSizeOf(String s) {
        return (s.length() * 2) + OBJ_OVERHEAD;
    }

    /**
     * We divide the file into small blocks. If the blocks are too small, we
     * shall create too many temporary files. If they are too big, we shall
     * be using too much memory.
     *
     * @param sizeOfFile   how much data (in bytes) can we expect
     * @param maxTempFiles how many temporary files can we create (e.g., 1024)
     * @param maxMemory    Maximum memory to use (in bytes)
     * @return the estimate
     */
    public static long estimateBestSizeOfBlocks(final long sizeOfFile,
                                                final int maxTempFiles, final long maxMemory) {
        // we don't want to open up much more than maxTempFiles temporary
        // files, better run out of memory first.
        long blockSize = sizeOfFile / maxTempFiles + (sizeOfFile % maxTempFiles == 0 ? 0 : 1);

        // on the other hand, we don't want to create many temporary files
        // for naught. If blockSize is smaller than half the free memory, grow it.
        if (blockSize < maxMemory / 2) {
            blockSize = maxMemory / 2;
        }
        return blockSize;
    }

    /**
     * Return the available free memory.
     *
     * @return The available free memory
     */
    public static long estimateAvailableMemory() {
        System.gc();
        return Runtime.getRuntime().freeMemory();
    }
}

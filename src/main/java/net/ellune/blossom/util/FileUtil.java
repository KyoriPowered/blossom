package net.ellune.blossom.util;

import java.io.File;

public final class FileUtil {

    /**
     * Delete a directory and its contents.
     *
     * @param directory The directory
     * @return The delete result
     */
    public static boolean deleteDirectory(final File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }

        return directory.delete();
    }
}

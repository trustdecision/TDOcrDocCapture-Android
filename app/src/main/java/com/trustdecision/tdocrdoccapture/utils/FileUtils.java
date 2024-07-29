package com.trustdecision.tdocrdoccapture.utils;

import android.content.Context;
import android.os.Environment;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public final class FileUtils {

    /**
     * Get the root directory of the SD card. If the SD card is not available, get the root directory of the internal storage.
     */
    public static File getRootPath() {
        File path = null;
        if (sdCardIsAvailable()) {
            path = Environment.getExternalStorageDirectory(); //SD root directory     /storage/emulated/0
        } else {
            path = Environment.getDataDirectory();//Internal storage root directory   /data
        }
        return path;
    }

    /**
     * Is the SD card available?
     */
    public static boolean sdCardIsAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sd = new File(Environment.getExternalStorageDirectory().getPath());
            return sd.canWrite();
        } else
            return false;
    }

    /**
     * Determine whether the directory exists. If it does not exist, determine whether it was created successfully.
     */
    public static boolean createOrExistsDir(String dirPath) {
        return createOrExistsDir(getFileByPath(dirPath));
    }

    /**
     * Determine whether the directory exists. If it does not exist, determine whether it was created successfully.
     */
    public static boolean createOrExistsDir(File file) {
        // If it exists, it returns true if it is a directory, false if it is a file,
        // and returns whether it was created successfully if it does not exist.
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * Determine whether the file exists, if not, determine whether it is created successfully
     */
    public static boolean createOrExistsFile(String filePath) {
        return createOrExistsFile(getFileByPath(filePath));
    }

    /**
     * Determine whether the file exists, if not, determine whether it is created successfully
     */
    public static boolean createOrExistsFile(File file) {
        if (file == null)
            return false;
        // 如果存在，是文件则返回true，是目录则返回false
        if (file.exists())
            return file.isFile();
        if (!createOrExistsDir(file.getParentFile()))
            return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get the file by path
     */
    public static File getFileByPath(String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    /**
     * Determines whether a string is null or contains only blank characters
     */
    private static boolean isSpace(final String s) {
        if (s == null)
            return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * closeable
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null)
            return;
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the directory of cached images
     */
    public static String getImageCacheDir(Context context) {
        File file;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            file = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {
            file = context.getCacheDir();
        }
        String path = file.getPath() + "/cache";
        File cachePath = new File(path);
        if (!cachePath.exists())
            cachePath.mkdir();
        return path;
    }

    /**
     * Delete all images in the cache image directory
     */
    public static void clearCache(Context context) {
        String cacheImagePath = getImageCacheDir(context);
        File cacheImageDir = new File(cacheImagePath);
        File[] files = cacheImageDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }

}

package com.trustdecision.tdocrdoccapture.utils;

public class CommonUtils {

    private static long lastClickTime;

    /**
     * Determine whether it is a quick click
     *
     * @return true，false
     */
    public static boolean isFastClick() {
        return isFastClick(1000);
    }

    /**
     * Determine whether it is a quick click
     *
     * @param intervalTime Interval time, in milliseconds.
     * @return true，false
     */
    public static boolean isFastClick(long intervalTime) {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < intervalTime) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}

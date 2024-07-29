package com.trustdecision.tdocrdoccapture.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Base64;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageUtils {

    public static boolean save(Bitmap src, String filePath, CompressFormat format) {
        return save(src, FileUtils.getFileByPath(filePath), format, false);
    }

    public static boolean save(Bitmap src, File file, CompressFormat format) {
        return save(src, file, format, false);
    }

    public static boolean save(Bitmap src, String filePath, CompressFormat format, boolean recycle) {
        return save(src, FileUtils.getFileByPath(filePath), format, recycle);
    }

    public static boolean save(Bitmap src, File file, CompressFormat format, boolean recycle) {
        if (isEmptyBitmap(src) || !FileUtils.createOrExistsFile(file)) {
            return false;
        }
//        System.out.println(src.getWidth() + ", " + src.getHeight());
        OutputStream os = null;
        boolean ret = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            ret = src.compress(format, 100, os);
            if (recycle && !src.isRecycled()) {
                src.recycle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtils.closeIO(os);
        }
        return ret;
    }

    /**
     * Determine whether the bitmap object is empty
     */
    private static boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }

    /**
     * Convert byte[] to Bitmap
     */
    public static Bitmap getBitmapFromByte(byte[] bytes, int width, int height) {
        final YuvImage image = new YuvImage(bytes, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream os = new ByteArrayOutputStream(bytes.length);
        if (!image.compressToJpeg(new Rect(0, 0, width, height), 100, os)) {
            return null;
        }
        byte[] tmp = os.toByteArray();
        Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
        return bmp;
    }

    //Rotate Bitmap
    public final static Bitmap rotate(Bitmap b, float degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
            if (b != b2) {
                b.recycle();
                b = b2;
            }

        }
        return b;
    }

    public static Bitmap compressBitmapByHeight(Bitmap bitmap, int maxHeight, int maxSize) {
        // Get the width and height of the original image
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        // Calculate the scaling factor
        float scale = (float) maxHeight / originalHeight;

        // Calculate the new width and height based on the scaling factor
        int newWidth = (int) (originalWidth * scale);
        int newHeight = maxHeight;

        Bitmap compressedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

        // Compress Bitmap to a specified size or less
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        while (baos.toByteArray().length / 1024 > maxSize) {
            baos.reset();
            quality -= 5;
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }

        byte[] byteArray = baos.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public static Bitmap compressBitmapByWidth(Bitmap bitmap, int maxWidth, int maxSize) {
        // Get the width and height of the original image
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        // Calculate the scaling factor
        float scale = (float) maxWidth / originalWidth;

        // Calculate the new width and height based on the scaling factor
        int newWidth = maxWidth;
        int newHeight = (int) (originalHeight * scale);

        Bitmap compressedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

        // Compress Bitmap to a specified size or less
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        while (baos.toByteArray().length / 1024 > maxSize) {
            baos.reset();
            quality -= 5;
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }

        byte[] byteArray = baos.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public static String getBase64FromImagePath(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        return imageToBase64(bitmap);
    }

    private static String imageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

}

package com.trustdecision.tdocrdoccapture.cropper;

import android.graphics.Bitmap;

/**
 * Clipping monitoring interface
 */
public interface CropListener {

    void onFinish(Bitmap bitmap);

}

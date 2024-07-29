package com.trustdecision.tdocrdoccapture.camera;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;

public class TDIDCardCamera {

    public final static int     TYPE_IDCARD_FACE_TIPS = 1;   //face tips
    public final static int     TYPE_IDCARD_NO_FACE_TIPS = 2;//no face tips
    public final static int     RESULT_CODE           = 0X11;
    public final static int     PERMISSION_CODE_FIRST = 0x12;
    public final static String  TAKE_TYPE             = "take_type";
    public final static String  IMAGE_PATH            = "image_path";

    private final WeakReference<Activity> mActivity;
    private final WeakReference<Fragment> mFragment;

    public static TDIDCardCamera create(Activity activity) {
        return new TDIDCardCamera(activity);
    }

    public static TDIDCardCamera create(Fragment fragment) {
        return new TDIDCardCamera(fragment);
    }

    private TDIDCardCamera(Activity activity) {
        this(activity, (Fragment) null);
    }

    private TDIDCardCamera(Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private TDIDCardCamera(Activity activity, Fragment fragment) {
        this.mActivity = new WeakReference(activity);
        this.mFragment = new WeakReference(fragment);
    }

    /**
     * Open Camera
     *
     * @param IDCardDirection（TYPE_IDCARD_FACE_TIPS / TYPE_IDCARD_NO_FACE_TIPS）
     */
    public void openCamera(int IDCardDirection) {
        Activity activity = this.mActivity.get();
        Fragment fragment = this.mFragment.get();
        Intent intent = new Intent(activity, TDCameraActivity.class);
        intent.putExtra(TAKE_TYPE, IDCardDirection);
        if (fragment != null) {
            fragment.startActivityForResult(intent, IDCardDirection);
        } else {
            activity.startActivityForResult(intent, IDCardDirection);
        }
    }

    /**
     * Get the image path
     *
     * @param data Intent
     * @return Image Path
     */
    public static String getImagePath(Intent data) {
        if (data != null) {
            return data.getStringExtra(IMAGE_PATH);
        }
        return "";
    }
}


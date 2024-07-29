package com.trustdecision.tdocrdoccapture.camera;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.trustdecision.tdocrdoccapture.R;
import com.trustdecision.tdocrdoccapture.cropper.CropImageView;
import com.trustdecision.tdocrdoccapture.utils.CommonUtils;
import com.trustdecision.tdocrdoccapture.cropper.CropListener;
import com.trustdecision.tdocrdoccapture.utils.FileUtils;
import com.trustdecision.tdocrdoccapture.utils.ImageUtils;
import com.trustdecision.tdocrdoccapture.utils.PermissionUtils;

import java.io.File;

public class TDCameraActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView       mIvCameraFlash;
    private TDCameraPreview mCameraPreview;
    private ImageView     mIvCameraCrop;
    private int           mType; //tips类型
    private Bitmap        mCropBitmap;
    private CropImageView mCropImageView;
    private View          mLlCameraResult;
    private View          mLlCameraButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Hide title bar */
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        /* Hide the status bar */
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setAttributes(lp);


        /*Dynamically request required permissions*/
        boolean checkPermissionFirst = PermissionUtils.checkPermissionFirst(this, TDIDCardCamera.PERMISSION_CODE_FIRST,
//                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
                new String[]{Manifest.permission.CAMERA});

        if (checkPermissionFirst) {
            init();
        }
    }

    private void init() {
        setContentView(R.layout.td_layout_camera);
        mType = getIntent().getIntExtra(TDIDCardCamera.TAKE_TYPE, 0);
        initView();
        initListener();
        /*Added a 0.1 second transition interface to solve the problem that the preview interface starts slowly when some phones apply for permissions for the first time*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCameraPreview.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 100);
    }

    /**
     * Handle the response to the permission request
     *
     * @param requestCode request code
     * @param permissions permissions array
     * @param grantResults request permission result array
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isPermissions = true;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                isPermissions = false;
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) { //用户选择了"不再询问"
                        Toast.makeText(this, "请手动打开该应用需要的权限", Toast.LENGTH_SHORT).show();
                    }
                }
        }
        if (isPermissions) {
            Log.d("onRequestPermission", "onRequestPermissionsResult: " + "允许所有权限");
            init();
        } else {
            Log.d("onRequestPermission", "onRequestPermissionsResult: " + "有权限不允许");
            finish();
        }
    }

    private void initView() {
        mCameraPreview = findViewById(R.id.td_camera_preview);
        mIvCameraFlash = findViewById(R.id.td_iv_camera_flash);
        mIvCameraCrop = findViewById(R.id.td_iv_camera_crop);
        mCropImageView = findViewById(R.id.td_crop_image_view);
        mLlCameraResult = findViewById(R.id.td_ll_camera_result);
        mLlCameraButton = findViewById(R.id.td_ll_camera_button);

        switch (mType) {
            case TDIDCardCamera.TYPE_IDCARD_FACE_TIPS:
                mIvCameraCrop.setImageResource(R.mipmap.td_05_2x);
                break;
            case TDIDCardCamera.TYPE_IDCARD_NO_FACE_TIPS:
                mIvCameraCrop.setImageResource(R.mipmap.td_06_2x);
                break;
        }
    }

    private void initListener() {
        mCameraPreview.setOnClickListener(this);
        mIvCameraFlash.setOnClickListener(this);
        findViewById(R.id.td_iv_camera_close).setOnClickListener(this);
        findViewById(R.id.td_iv_camera_take).setOnClickListener(this);
        findViewById(R.id.td_tv_camera_result_ok).setOnClickListener(this);
        findViewById(R.id.tv_camera_result_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.td_camera_preview) {
            mCameraPreview.focus();
        } else if (id == R.id.td_iv_camera_close) {
            finish();
        } else if (id == R.id.td_iv_camera_take) {
            if (!CommonUtils.isFastClick()) {
                takePhoto();
            }
        } else if (id == R.id.td_iv_camera_flash) {
            if (CameraUtils.hasFlash(this)) {
                boolean isFlashOn = mCameraPreview.switchFlashLight();
                mIvCameraFlash.setImageResource(isFlashOn ? R.mipmap.td_open_falshlight : R.mipmap.td_close_falshlight);
            } else {
                Toast.makeText(this, R.string.td_no_flash, Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.td_tv_camera_result_ok) {
            confirm();
        } else if (id == R.id.tv_camera_result_cancel) {
            mCameraPreview.setEnabled(true);
            mCameraPreview.addCallback();
            mCameraPreview.startPreview();
            mIvCameraFlash.setImageResource(R.mipmap.td_close_falshlight);
            setTakePhotoLayout();
        }
    }

    /**
     * takePhoto
     */
    private void takePhoto() {
        mCameraPreview.setEnabled(false);
        CameraUtils.getCamera().setOneShotPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] bytes, Camera camera) {
                final Camera.Size size = camera.getParameters().getPreviewSize(); //获取预览大小
                camera.stopPreview();

                Camera.Parameters parameters = camera.getParameters();
                int rotation = CameraUtils.getDisplayOrientation(TDCameraActivity.this);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                         int w = size.width;
                         int h = size.height;
                        Bitmap bitmap = ImageUtils.getBitmapFromByte(bytes, w, h);
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                            if (size.width > size.height){
                                bitmap = ImageUtils.rotate(bitmap,rotation);
                            }
                        }
                        cropImage(bitmap);
                    }
                }).start();
            }
        });
    }

    /**
     * Crop the image
     */
    private void cropImage(Bitmap bitmap) {
        /*Calculate the coordinate points of the scan box*/
        float left = mIvCameraCrop.getX();
        float right = mIvCameraCrop.getRight();
        float top = mIvCameraCrop.getTop();
        float bottom = mIvCameraCrop.getBottom();

        /*Calculate the ratio of the scan frame coordinate points to the original image coordinate points*/
        float leftProportion = left / mCameraPreview.getWidth();
        float rightProportion = right   / mCameraPreview.getWidth();
        float topProportion = top / mCameraPreview.getHeight();
        float bottomProportion = bottom / mCameraPreview.getBottom();

        // Enlarge the proportion
        float expansion = 0.05f; // 5%
        leftProportion = Math.max(0, leftProportion - expansion);
        rightProportion = Math.min(1, rightProportion + expansion );
        topProportion = Math.max(0, topProportion - expansion / 3);      //Because the border itself is large, it only expands by one third
        bottomProportion = Math.min(1, bottomProportion + expansion / 3);//Because the border itself is large, it only expands by one third

        // Automatic cropping
        int cropX = (int) (leftProportion * bitmap.getWidth());
        int cropY = (int) (topProportion * bitmap.getHeight());
        int cropWidth = (int) ((rightProportion - leftProportion) * bitmap.getWidth());
        int cropHeight = (int) ((bottomProportion - topProportion) * bitmap.getHeight());

        // Bounds Checking
        cropX = Math.max(0, cropX);
        cropY = Math.max(0, cropY);
        cropWidth = Math.min(bitmap.getWidth() - cropX, cropWidth);
        cropHeight = Math.min(bitmap.getHeight() - cropY, cropHeight);

        mCropBitmap = Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight);

        /* Set to manual cropping mode */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Set the manual crop area to be the same size as the scan box
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mIvCameraCrop.getWidth(), mIvCameraCrop.getHeight());
                mCropImageView.setX(mIvCameraCrop.getX());
                mCropImageView.setY(mIvCameraCrop.getY());
                mCropImageView.setLayoutParams(layoutParams);
                setCropLayout();
                /**
                 *
                 * @param showClipPoint true: Show Clipping Points, false: Do not show crop points
                 * **/
                mCropImageView.setImageBitmap(mCropBitmap, true);
            }
        });
    }

    /**
     * Set the crop layout
     */
    private void setCropLayout() {
        mIvCameraCrop.setVisibility(View.GONE);
        mCameraPreview.setVisibility(View.GONE);
        mLlCameraButton.setVisibility(View.GONE);
        mCropImageView.setVisibility(View.VISIBLE);
        mLlCameraResult.setVisibility(View.VISIBLE);
    }

    /**
     * Set up photo layout
     */
    private void setTakePhotoLayout() {
        mIvCameraCrop.setVisibility(View.VISIBLE);
        mCameraPreview.setVisibility(View.VISIBLE);
        mLlCameraButton.setVisibility(View.VISIBLE);
        mCropImageView.setVisibility(View.GONE);
        mLlCameraResult.setVisibility(View.GONE);

        mCameraPreview.focus();
    }

    /**
     * Click OK to return to the image path
     */
    private void confirm() {
        /* Manually crop an image */
        mCropImageView.crop(new CropListener() {
            @Override
            public void onFinish(Bitmap bitmap) {
                if (bitmap == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.td_crop_fail), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                //压缩图片
                bitmap =  ImageUtils.compressBitmapByHeight(bitmap,500,300);

                /*Save the image to sdcard and return the image path*/
                String imagePath = FileUtils.getImageCacheDir(TDCameraActivity.this) + File.separator +
                        "td_" + System.currentTimeMillis() + ".jpg";
                if (ImageUtils.save(bitmap, imagePath, Bitmap.CompressFormat.JPEG)) {
                    Intent intent = new Intent();
                    intent.putExtra(TDIDCardCamera.IMAGE_PATH, imagePath);
                    setResult(TDIDCardCamera.RESULT_CODE, intent);
                    finish();
                }
            }
        }, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCameraPreview != null) {
            mCameraPreview.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraPreview != null) {
            mCameraPreview.onStop();
        }
    }

}

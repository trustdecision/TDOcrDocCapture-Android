package com.trustdecision.tdocrdoccapturedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trustdecision.tdocrdoccapture.camera.TDIDCardCamera;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ImageView mIvIDCardResult;
    private TextView mTVinfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIvIDCardResult =  findViewById(R.id.iv_front);
        mTVinfo =  findViewById(R.id.tv_info);

        LinearLayout noFaceTips = findViewById(R.id.no_face_tips);
        /*
         * TDIDCardCamera.TYPE_IDCARD_NO_FACE_TIPS
         */
        noFaceTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TDIDCardCamera.create(MainActivity.this).openCamera(TDIDCardCamera.TYPE_IDCARD_NO_FACE_TIPS);
            }
        });


        /*
         * TDIDCardCamera.TYPE_IDCARD_FACE_TIPS
         */
        LinearLayout faceTips = findViewById(R.id.face_tips);
        faceTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TDIDCardCamera.create(MainActivity.this).openCamera(TDIDCardCamera.TYPE_IDCARD_FACE_TIPS);
            }
        });

//        faceTips.performClick();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == TDIDCardCamera.RESULT_CODE) {
            //Get the image path and display the image
            final String path = TDIDCardCamera.getImagePath(data);
            if (!TextUtils.isEmpty(path)) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                mIvIDCardResult.setImageBitmap(bitmap);

                mTVinfo.setText(Html.fromHtml("<font color='#12B455'><big><b>Image Info: </b></big></font>"));
                mTVinfo.append( "Width:"+bitmap.getWidth() +", Height:"+bitmap.getHeight());

                File file = new File(path);
                if (file.exists() && file.isFile()){
                    double fileSize = (double) file.length() / 1024;
                    mTVinfo.append("\n");
                    mTVinfo.append(Html.fromHtml("<font color='#12B455'><big><b>Image Size: </b></big></font>"));
                    mTVinfo.append(String.format("%.2f KB", fileSize));
                }

                mTVinfo.append("\n");
                mTVinfo.append(Html.fromHtml("<font color='#12B455'><big><b>Image Path: </b></big></font>"));
                mTVinfo.append(path);

                // upload image...
//                String imageBase64 = ImageUtils.getBase64FromImagePath(path);


                // In actual development, after successfully uploading the image to the server, you need to delete all cached images. Just call the following method:
//                FileUtils.clearCache(this);
            }
        }
    }
}
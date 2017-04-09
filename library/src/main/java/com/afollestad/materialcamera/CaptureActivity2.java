package com.afollestad.materialcamera;

import android.support.annotation.NonNull;

import com.afollestad.materialcamera.internal.BaseCameraFragment;
import com.afollestad.materialcamera.internal.BaseCaptureActivity;
import com.afollestad.materialcamera.internal.Camera2Fragment;

public class CaptureActivity2 extends BaseCaptureActivity {

    @Override
    @NonNull
    public BaseCameraFragment newFragment() {
        return Camera2Fragment.newInstance();
    }
}
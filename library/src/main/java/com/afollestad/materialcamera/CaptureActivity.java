package com.afollestad.materialcamera;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.afollestad.materialcamera.internal.BaseCaptureActivity;
import com.afollestad.materialcamera.internal.CameraFragment;
import com.afollestad.materialcamera.internal.CaptureButton;

public class CaptureActivity extends BaseCaptureActivity implements CaptureButton.CaptureListener{
    CameraFragment mFragment;
    CaptureButton mCaptureBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCaptureBtn= (CaptureButton) findViewById(R.id.mCaptureBtn);
        mCaptureBtn.setCaptureListener(this);
    }

    @Override
    @NonNull
    public Fragment newFragment() {
        mFragment=CameraFragment.newInstance();
        return mFragment;
    }

    @Override
    public void capture() {
        mFragment.takeStillshot();
        mCaptureBtn.captureSuccess();
    }

    @Override
    public void cancel() {
        onRetry(null);
    }

    @Override
    public void determine() {

    }

    @Override
    public void quit() {

    }

    @Override
    public void record() {

    }

    @Override
    public void rencodEnd() {

    }

    @Override
    public void getRecordResult() {

    }

    @Override
    public void deleteRecordResult() {

    }

    @Override
    public void scale(float scaleValue) {

    }
}
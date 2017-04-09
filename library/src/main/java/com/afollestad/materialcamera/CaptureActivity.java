package com.afollestad.materialcamera;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.afollestad.materialcamera.internal.BaseCaptureActivity;
import com.afollestad.materialcamera.internal.CameraFragment;
import com.afollestad.materialcamera.internal.CaptureButton;
import com.cy.app.Log;

public class CaptureActivity extends BaseCaptureActivity implements CaptureButton.CaptureListener{
    CaptureButton mCaptureBtn;
    public static final int REQUEST_CODE_MEDIA=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCaptureBtn= (CaptureButton) findViewById(R.id.mCaptureBtn);
        mCaptureBtn.setCaptureListener(this);
    }

    @Override
    @NonNull
    public CameraFragment newFragment() {
        CameraFragment mFragment=CameraFragment.newInstance();
        return mFragment;
    }

    @Override
    public void capture() {
        getCameraFragment().takeStillshot();
        mCaptureBtn.captureSuccess();
    }

    @Override
    public void cancel() {//拍照预览按界面 return 按钮
        Log.w(CamConst.LOG_CALLBACK_FLOW,"cancel");

//        onRetry(getPreviewInstance().getOutputUri());
    }

    @Override
    public void determine() {//拍照预览按界面 确认使用 按钮
        Log.w(CamConst.LOG_CALLBACK_FLOW,"determine:"+ getBasePreviewFra().getOutputUri());
        finishBackResult(getBasePreviewFra().getOutputUri());
    }

    @Override
    public void quit() {
        Log.w(CamConst.LOG_CALLBACK_FLOW,"quit");
    }

    @Override
    public void record() {//录像开始
        Log.w(CamConst.LOG_CALLBACK_FLOW,"record");
        getCameraFragment().startRecordingVideo();
    }

    @Override
    public void rencodEnd() {//录像结束
        Log.w(CamConst.LOG_CALLBACK_FLOW,"rencodEnd");
        getCameraFragment().stopRecordingVideo(true);
    }

    @Override
    public void getRecordResult() {//录像预览按界面 确认使用 按钮
        Log.w(CamConst.LOG_CALLBACK_FLOW,"getRecordResult:"+ getBasePreviewFra().getOutputUri());
        finishBackResult(getBasePreviewFra().getOutputUri());
    }

    private void finishBackResult(String mediaPath) {
        Intent intent=new Intent();
        intent.putExtra("data",mediaPath);
        setResult(REQUEST_CODE_MEDIA,intent);
        finish();
    }

    @Override
    public void deleteRecordResult() {//录像预览按界面 return 按钮
        Log.w(CamConst.LOG_CALLBACK_FLOW,"deleteRecordResult");
    }

    @Override
    public void scale(float scaleValue) {
        Log.w(CamConst.LOG_CALLBACK_FLOW,"scale:"+scaleValue);
    }
}
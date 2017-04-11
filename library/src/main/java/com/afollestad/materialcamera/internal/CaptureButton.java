package com.afollestad.materialcamera.internal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.afollestad.materialcamera.R;
import com.cy.app.UtilContext;

import java.security.KeyFactory;

import static android.R.attr.width;

/**
 * {@link #captureSuccess() 拍照完成需要调用执行的动画}
 */
public class CaptureButton extends View {

    public final String TAG = "CaptureButtom";

    private Paint mPaint;
    private Context mContext;

    private float btn_center_Y;
    private float btn_center_X;

    private float btn_inside_radius;
    private float btn_outside_radius;
    //before radius
    private float btn_before_inside_radius;
    private float btn_before_outside_radius;
    //after radius
    /**放大后内圈半径*/
    private float btn_after_inside_radius;
    /**放大后外圈半径*/
    private float btn_after_outside_radius;

    private float btn_return_length;
    private float btn_return_X;
    private float btn_return_Y;

    private float btn_left_X, btn_right_X, btn_result_radius;

    //state
    private int stateSelected;
    private final int STATE_LESSNESS = 0;
    private final int STATE_KEY_DOWN = 1;
    private final int STATE_CAPTURED = 2;
    private final int STATE_RECORD = 3;
    /**正在预览已拍图片*/
    private final int STATE_PICTURE_BROWSE = 4;
    /**正在预览已拍视频*/
    private final int STATE_RECORD_BROWSE = 5;
    private final int STATE_READYQUIT = 6;
    private final int STATE_RECORDED = 7;

    private float key_down_Y;

    private RectF rectF;
    private float progress = 0;
    private LongPressRunnable longPressRunnable = new LongPressRunnable();
    private RecordRunnable recordRunnable = new RecordRunnable();
    private ValueAnimator record_anim = ValueAnimator.ofFloat(0, 360);
    private CaptureListener mCaptureListener;

    public CaptureButton(Context context) {
        this(context, null);
    }

    public CaptureButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        stateSelected = STATE_LESSNESS;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = widthSize;
        Log.i(TAG, "measureWidth = " + width);
        int height = (width / 9) * 4;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        btn_center_X = getWidth() / 2;
        btn_center_Y = getHeight() / 2;

        btn_outside_radius = (float) dp(35);
        btn_inside_radius = (float) dp(29);

        btn_before_outside_radius = btn_outside_radius;
        btn_before_inside_radius = btn_inside_radius;
        btn_after_outside_radius = (float)dp(45);
        btn_after_inside_radius = (float) (btn_outside_radius * 0.6);

        btn_return_length = (float) (btn_outside_radius * 0.35);
        btn_result_radius = dp(38);
        btn_left_X = getWidth() / 2;
        btn_right_X = getWidth() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (stateSelected == STATE_LESSNESS || stateSelected == STATE_RECORD) {
            //draw capture button
            //画外环
            mPaint.setColor(0xFFEEEEEE);
            mPaint.setStrokeWidth(dp(7));
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(btn_center_X, btn_center_Y, btn_outside_radius, mPaint);
            //画内环
            if (btn_outside_radius==btn_after_outside_radius){
                mPaint.setColor(0xff16cd90);
            }else {
                mPaint.setColor(0x33fafafa);
            }
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(btn_center_X, btn_center_Y, btn_inside_radius, mPaint);

            //draw Progress bar
            Paint paintArc = new Paint();
            paintArc.setAntiAlias(true);
            paintArc.setColor(0xff16cd90);
            paintArc.setStyle(Paint.Style.STROKE);
            paintArc.setStrokeWidth(dp(7));

            rectF = new RectF(btn_center_X - (btn_after_outside_radius ),
                    btn_center_Y - (btn_after_outside_radius ),
                    btn_center_X + (btn_after_outside_radius ),
                    btn_center_Y + (btn_after_outside_radius ));
            canvas.drawArc(rectF, -90, progress, false, paintArc);

//            drawDownArrow(canvas);
        } else if (stateSelected == STATE_RECORD_BROWSE || stateSelected == STATE_PICTURE_BROWSE) {

            drawReturnConfirmBtn(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (stateSelected == STATE_LESSNESS) {
                    if (event.getY() > btn_return_Y - 37 &&
                            event.getY() < btn_return_Y + 10 &&
                            event.getX() > btn_return_X - 37 &&
                            event.getX() < btn_return_X + 37) {
                        stateSelected = STATE_READYQUIT;
                    } else if (event.getY() > btn_center_Y - btn_outside_radius &&
                            event.getY() < btn_center_Y + btn_outside_radius &&
                            event.getX() > btn_center_X - btn_outside_radius &&
                            event.getX() < btn_center_X + btn_outside_radius &&
                            event.getPointerCount() == 1
                            ) {
                        if(!FileUtil.isExternalStorageWritable()){
                            Toast.makeText(mContext,"请插入储存卡", Toast.LENGTH_SHORT).show();
                        }else{
                            key_down_Y = event.getY();
                            stateSelected = STATE_KEY_DOWN;
                            postCheckForLongTouch();
                        }
                    }
                } else if (stateSelected == STATE_RECORD_BROWSE || stateSelected == STATE_PICTURE_BROWSE) {
                    if (event.getY() > btn_center_Y - btn_result_radius &&
                            event.getY() < btn_center_Y + btn_result_radius &&
                            event.getX() > btn_left_X - btn_result_radius &&
                            event.getX() < btn_left_X + btn_result_radius &&
                            event.getPointerCount() == 1
                            ) {
                        if (mCaptureListener != null) {

                            if (stateSelected == STATE_RECORD_BROWSE) {
                                mCaptureListener.deleteRecordResult();
                            } else if (stateSelected == STATE_PICTURE_BROWSE) {
                                mCaptureListener.cancel();
                            }
                        }
                        stateSelected = STATE_LESSNESS;
                        btn_left_X = btn_center_X;
                        btn_right_X = btn_center_X;
                        invalidate();
                    } else if (event.getY() > btn_center_Y - btn_result_radius &&
                            event.getY() < btn_center_Y + btn_result_radius &&
                            event.getX() > btn_right_X - btn_result_radius &&
                            event.getX() < btn_right_X + btn_result_radius &&
                            event.getPointerCount() == 1
                            ) {
                        if (mCaptureListener != null) {
                            if (stateSelected == STATE_RECORD_BROWSE) {
                                mCaptureListener.getRecordResult();
                            } else if (stateSelected == STATE_PICTURE_BROWSE) {
                                mCaptureListener.determine();
                            }
                        }
                        stateSelected = STATE_LESSNESS;
                        btn_left_X = btn_center_X;
                        btn_right_X = btn_center_X;
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getY() > btn_center_Y - btn_outside_radius &&
                        event.getY() < btn_center_Y + btn_outside_radius &&
                        event.getX() > btn_center_X - btn_outside_radius &&
                        event.getX() < btn_center_X + btn_outside_radius
                        ) {
                }
                if (mCaptureListener != null) {
                    mCaptureListener.scale(key_down_Y - event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                removeCallbacks(longPressRunnable);
                if (stateSelected == STATE_READYQUIT) {
                    if (event.getY() > btn_return_Y - 37 &&
                            event.getY() < btn_return_Y + 10 &&
                            event.getX() > btn_return_X - 37 &&
                            event.getX() < btn_return_X + 37) {
                        stateSelected = STATE_LESSNESS;
                        if (mCaptureListener != null) {
                            mCaptureListener.quit();
                        }
                    }
                } else if (stateSelected == STATE_KEY_DOWN) {
                    if (event.getY() > btn_center_Y - btn_outside_radius &&
                            event.getY() < btn_center_Y + btn_outside_radius &&
                            event.getX() > btn_center_X - btn_outside_radius &&
                            event.getX() < btn_center_X + btn_outside_radius) {
                        if (mCaptureListener != null) {
                            mCaptureListener.capture();
                        }
                        stateSelected = STATE_PICTURE_BROWSE;
                    }
                } else if (stateSelected == STATE_RECORD) {
                    if (record_anim.getCurrentPlayTime() < 500) {
                        stateSelected = STATE_LESSNESS;
//                        Toast.makeText(mContext, "Under time", Toast.LENGTH_SHORT).show();
                        progress = 0;
                        invalidate();
                        record_anim.cancel();
                    } else {
                        stateSelected = STATE_RECORD_BROWSE;
                        removeCallbacks(recordRunnable);
//                        Toast.makeText(mContext, "Time length " + record_anim.getCurrentPlayTime(), Toast.LENGTH_SHORT).show();
                        captureAnimation(getWidth() / 5, (getWidth() / 5) * 4);
                        record_anim.cancel();
                        progress = 0;
                        invalidate();
                        if (mCaptureListener != null) {
                            mCaptureListener.rencodEnd();
                        }
                    }
                    if (btn_outside_radius == btn_after_outside_radius && btn_inside_radius == btn_after_inside_radius) {
//                            startAnimation(btn_outside_radius, btn_outside_radius - 40, btn_inside_radius, btn_inside_radius + 20);
                        startAnimation(btn_after_outside_radius, btn_before_outside_radius, btn_after_inside_radius, btn_before_inside_radius);
                    } else {
                        startAnimation(btn_after_outside_radius, btn_before_outside_radius, btn_after_inside_radius, btn_before_inside_radius);
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 播放拍照完成动画
     */
    public void captureSuccess() {
        captureAnimation(getWidth() / 5, (getWidth() / 5) * 4);
    }

    private void postCheckForLongTouch() {
        postDelayed(longPressRunnable, 500);
    }


    private class LongPressRunnable implements Runnable {
        @Override
        public void run() {
                startAnimation(btn_before_outside_radius, btn_after_outside_radius, btn_before_inside_radius, btn_after_inside_radius);
                stateSelected = STATE_RECORD;
        }
    }

    private class RecordRunnable implements Runnable {
        @Override
        public void run() {
            if (mCaptureListener != null) {
                mCaptureListener.record();
            }
            record_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (stateSelected == STATE_RECORD) {
                        progress =  (float)animation.getAnimatedValue();
                    }
                    invalidate();
                }
            });
            record_anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (stateSelected == STATE_RECORD) {
                        stateSelected = STATE_RECORD_BROWSE;
                        progress = 0;
                        invalidate();
                        captureAnimation(getWidth() / 5, (getWidth() / 5) * 4);
                        if (btn_outside_radius == btn_after_outside_radius && btn_inside_radius == btn_after_inside_radius) {
                            startAnimation(btn_after_outside_radius, btn_before_outside_radius, btn_after_inside_radius, btn_before_inside_radius);
                        } else {
                            startAnimation(btn_after_outside_radius, btn_before_outside_radius, btn_after_inside_radius, btn_before_inside_radius);
                        }
                        if (mCaptureListener != null) {
                            mCaptureListener.rencodEnd();
                        }
                    }
                }
            });
            record_anim.setInterpolator(new LinearInterpolator());
            record_anim.setDuration(10000);
            record_anim.start();
        }
    }

    private void startAnimation(float outside_start, float outside_end, float inside_start, float inside_end) {

        ValueAnimator outside_anim = ValueAnimator.ofFloat(outside_start, outside_end);
        ValueAnimator inside_anim = ValueAnimator.ofFloat(inside_start, inside_end);
        outside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btn_outside_radius = (float) animation.getAnimatedValue();
                invalidate();
            }

        });
        outside_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (stateSelected == STATE_RECORD) {
                        postDelayed(recordRunnable, 100);
                }
            }
        });
        inside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btn_inside_radius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        outside_anim.setDuration(100);
        inside_anim.setDuration(100);
        outside_anim.start();
        inside_anim.start();
    }

    private void captureAnimation(float left, float right) {
        Log.i("CaptureButtom", left + "==" + right);
        ValueAnimator left_anim = ValueAnimator.ofFloat(btn_left_X, left);
        ValueAnimator right_anim = ValueAnimator.ofFloat(btn_right_X, right);
        left_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btn_left_X = (float) animation.getAnimatedValue();
                Log.i("CJT", btn_left_X + "=====");
                invalidate();
            }

        });
        right_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btn_right_X = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        left_anim.setDuration(200);
        right_anim.setDuration(200);
        left_anim.start();
        right_anim.start();
    }

    public void setCaptureListener(CaptureListener mCaptureListener) {
        this.mCaptureListener = mCaptureListener;
    }


    public interface CaptureListener {
        public void capture();

        public void cancel();

        public void determine();

        public void quit();

        public void record();

        public void rencodEnd();

        public void getRecordResult();

        public void deleteRecordResult();

        public void scale(float scaleValue);
    }

    private void drawReturnConfirmBtn(Canvas canvas) {
        drawDrawable(canvas,R.drawable.mediarecord_return,dp(40),getHeight()-dp(121),dp(76));
        drawDrawable(canvas,R.drawable.mediarecord_confirm,getWidth()-dp(116),getHeight()-dp(121),dp(76));

       /* if (true) return;
        mPaint.setColor(0xcd979797);
        canvas.drawCircle(btn_left_X, btn_center_Y, btn_result_radius, mPaint);
        mPaint.setColor(0xff16cd90);
        canvas.drawCircle(btn_right_X, btn_center_Y, btn_result_radius, mPaint);


        //left button 返回拐角箭头
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        Path path = new Path();

        path.moveTo(btn_left_X - 2, btn_center_Y + 14);
        path.lineTo(btn_left_X + 14, btn_center_Y + 14);
        path.arcTo(new RectF(btn_left_X, btn_center_Y - 14, btn_left_X + 28, btn_center_Y + 14), 90, -180);
        path.lineTo(btn_left_X - 14, btn_center_Y - 14);
        canvas.drawPath(path, paint);


        paint.setStyle(Paint.Style.FILL);
        path.reset();
        path.moveTo(btn_left_X - 14, btn_center_Y - 22);
        path.lineTo(btn_left_X - 14, btn_center_Y - 6);
        path.lineTo(btn_left_X - 23, btn_center_Y - 14);
        path.close();
        canvas.drawPath(path, paint);

        //打钩
        paint.setStyle(Paint.Style.STROKE);
//            paint.setColor(0xFF00CC00);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(dp(2));
        path.reset();
        path.moveTo(btn_right_X - 28, btn_center_Y);
        path.lineTo(btn_right_X - 8, btn_center_Y + 22);
        path.lineTo(btn_right_X + 30, btn_center_Y - 20);
        path.lineTo(btn_right_X - 8, btn_center_Y + 18);
        path.close();
        canvas.drawPath(path, paint);*/
    }

    private void drawDownArrow(Canvas canvas){
        //draw return button 下箭头
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            Path path = new Path();

            btn_return_X = ((getWidth() / 2) - btn_outside_radius) / 2;
            btn_return_Y = (getHeight() / 2 + 10);

            path.moveTo(btn_return_X - btn_return_length, btn_return_Y - btn_return_length);
            path.lineTo(btn_return_X, btn_return_Y);
            path.lineTo(btn_return_X + btn_return_length, btn_return_Y - btn_return_length);
            canvas.drawPath(path, paint);
    }

    public static int dp(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,
                UtilContext.getContext().getResources().getDisplayMetrics());
    }

    public static void drawDrawable(Canvas canvas,int drawableId,int left,int top,int width_height){
        drawDrawable(canvas,drawableId,left,top,width_height,width_height);
    }

    public static void drawDrawable(Canvas canvas,int drawableId,int left,int top,int width,int height){
        RectF rectF=new RectF(left,top,left+width,top+height);
        Bitmap bmp = BitmapFactory.decodeResource(UtilContext.getContext().getResources(), drawableId);
        canvas.drawBitmap(bmp,null,rectF,null);
    }
}

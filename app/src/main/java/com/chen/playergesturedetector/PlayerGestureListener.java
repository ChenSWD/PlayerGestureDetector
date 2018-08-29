package com.chen.playergesturedetector;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

/**
 * 播放器常用手势监听:单击、双击、横向滑动、左右两边边竖向滑动(亮度和声音)
 */

public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
    private WeakReference<VideoGestureListener> listener;
    private int screenWidth, centerW;
    //播放器View的长宽DP
    private float dpVideoWidth, dpVideoHeight;
    private ScrollMode scrollMode = ScrollMode.NONE;
    private long timeStamp;
    private int mTouchSlop;
    private int scrollRatio = 1;    //快进的比率，速度越快，值越大
    private int preDpVideoDuration = 0;     //每一dp，快进的时长,ms
    private float density;
    private int totalDuration = 0;      //单次快进快退累计值
    private float leftTBValue = 0;      //单次左边累计值(一般是亮度)
    private float rightTBValue = 0;     //单次右边累计值(一般是声音)

    public PlayerGestureListener(VideoGestureListener listener, Context context) {
        this.listener = new WeakReference<>(listener);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        screenWidth = outMetrics.widthPixels;
        centerW = screenWidth / 2;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        density = context.getResources().getDisplayMetrics().density;
    }

    //设置播放器SurfaceView的宽高
    public void setVideoWH(int w, int h) {
        dpVideoWidth = w / density;
        dpVideoHeight = h / density;
        //默认基础总共2分钟，代表的意义：从屏幕一边滑动到另一边，总共可以快进2分钟
        preDpVideoDuration = (int) (2 * 60 * 1000 / dpVideoWidth);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        scrollMode = ScrollMode.NONE;
        timeStamp = System.currentTimeMillis();
        totalDuration = 0;
        leftTBValue = 0;
        rightTBValue = 0;
        if (listener.get() != null) {
            listener.get().onGestureDown();
        }
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return super.onSingleTapUp(e);
    }

    @Override
    public void onShowPress(MotionEvent e) {
        super.onShowPress(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        long time = System.currentTimeMillis();
        distanceX = -distanceX;
        distanceY = -distanceY;
        float dpX = distanceX / density;
        float dpY = distanceY / density;
        updateScrollRatio(dpX, time - timeStamp);
        timeStamp = time;
        float xDiff = e2.getX() - e1.getX();
        float yDiff = e2.getY() - e1.getY();
        if (scrollMode == ScrollMode.NONE) {
            //横向滑动
            if (Math.abs(xDiff) > mTouchSlop) {
                scrollMode = ScrollMode.HORIZONTAL_S;
                updateVideoTime((int) (preDpVideoDuration * xDiff));
            }
            //纵向滑动
            else if (Math.abs(yDiff) > mTouchSlop) {
                if (e1.getX() < centerW) {
                    scrollMode = ScrollMode.LEFT_TB;
                } else {
                    scrollMode = ScrollMode.RIGHT_TB;
                }
            }
        }
        //快进快退
        else if (scrollMode == ScrollMode.HORIZONTAL_S) {
            updateVideoTime((int) (preDpVideoDuration * scrollRatio * dpX));
        } else if (scrollMode == ScrollMode.LEFT_TB) {
            updateVideoLeftTB(dpY / dpVideoHeight);
        } else if (scrollMode == ScrollMode.RIGHT_TB) {
            updateVideoRightTB(dpY / dpVideoHeight);
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (listener.get() != null) {
            listener.get().onGestureDoubleClick();
        }
        //双击事件
        return super.onDoubleTap(e);
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return super.onDoubleTapEvent(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (listener.get() != null) {
            listener.get().onGestureSingleClick();
        }
        //单击事件，在双击事件发生时不会产生这个事件，所以用这个回调作为播放器单击事件
        return super.onSingleTapConfirmed(e);
    }

    /***
     * 根据滑动速度更新速度比率值，这样可以在滑动速率较快时，快进的速度也会变快，可以根据需要调整
     * 根据实验，正常速度一般在10~40dp/s
     * @param dpX：横向滑动距离 dp
     * @param duration：时间间隔 ms
     */
    private void updateScrollRatio(float dpX, long duration) {
        int ratio = (int) ((Math.abs(dpX) / duration) * 1000);
        if (ratio < 20) {
            scrollRatio = 1;
        } else if (ratio < 40) {
            scrollRatio = 3;
        } else if (ratio < 70) {
            scrollRatio = 7;
        } else if (ratio < 100) {
            scrollRatio = 13;
        } else if (ratio < 300) {
            scrollRatio = 18;
        } else if (ratio < 500) {
            scrollRatio = 24;
        } else if (ratio < 800) {
            scrollRatio = 31;
        } else if (ratio < 1000) {
            scrollRatio = 40;
        } else {
            scrollRatio = 60;
        }
    }

    //累积快进进度,totalDuration：当前快进的总值，负值代表是要快退
    private void updateVideoTime(int duration) {
        totalDuration += duration;
        if (listener.get() != null) {
            listener.get().onGestureUpdateVideoTime(totalDuration);
        }
    }

    //累积亮度
    private void updateVideoLeftTB(float ratio) {
        leftTBValue += ratio;
        if (listener.get() != null) {
            listener.get().onGestureLeftTB(leftTBValue);
        }
    }

    //累积声音
    private void updateVideoRightTB(float ratio) {
        rightTBValue += ratio;
        if (listener.get() != null) {
            listener.get().onGestureRightTB(rightTBValue);
        }
    }

    public interface VideoGestureListener {
        /***
         * 手指在Layout左半部上下滑动时候调用，一般是亮度手势
         * 从View底部滑动到顶部，代表从0升到1
         * @param ratio：0-1 之间，1代表最亮，0代表最暗
         */
        void onGestureLeftTB(float ratio);

        /***
         * 手指在Layout右半部上下滑动时候调用，一般是音量手势
         * 从View底部滑动到顶部，代表从0升到1
         * @param ratio：0-1 之间，1代表音量最大，0代表音量最低
         */
        void onGestureRightTB(float ratio);

        /**
         * @param duration :快进快退,大于0快进，小于0快退
         */
        void onGestureUpdateVideoTime(int duration);

        //单击手势，确认是单击的时候调用
        void onGestureSingleClick();

        //双击手势，确认是双击的时候调用，可用于播放器暂停
        void onGestureDoubleClick();

        void onGestureDown();
    }

    private enum ScrollMode {
        NONE,               //初始值
        LEFT_TB,            //左边上下滑动(调节亮度)
        RIGHT_TB,           //右边上下滑动(调节声音)
        HORIZONTAL_S,       //横向滑动(快进快退)
        SINGLE_CLICK,       //单击
        DOUBLE_CLICK        //双击
    }
}

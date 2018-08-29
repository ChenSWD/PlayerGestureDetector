package com.chen.playergesturedetector;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, PlayerGestureListener.VideoGestureListener {

    private GestureDetectorCompat mDetector;
    private PlayerGestureListener playerGestureListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //监听相关手势
        mDetector = new GestureDetectorCompat(this, playerGestureListener = new PlayerGestureListener(this, this));
        findViewById(R.id.player_view).setOnTouchListener(this);
        //这里随便写了宽高，实际应该是播放器的长宽：player_view 的长宽
        playerGestureListener.setVideoWH(1000, 600);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            //如果在快进状态，则检测到这两个手势代表快进结束：因为GestureDetectorCompat不能检测手势抬起的缘故，所以要自己检测
            //做一些操作：如播放器快进 seekTo()
        }
        return mDetector.onTouchEvent(event);
    }

    @Override
    public void onGestureLeftTB(float ratio) {
    }

    @Override
    public void onGestureRightTB(float ratio) {
    }

    @Override
    public void onGestureUpdateVideoTime(int duration) {
    }

    @Override
    public void onGestureSingleClick() {
    }

    @Override
    public void onGestureDoubleClick() {
    }

    @Override
    public void onGestureDown() {
    }
}

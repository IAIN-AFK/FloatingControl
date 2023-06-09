package com.android.floatingcontrol;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class ControlService extends AccessibilityService {
    private static final String TAG = "ControlService";
    private ControlService.ControlBinder mBinder = new ControlBinder();
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private View displayView;
    private Context mContext;
    private AccessibilityNodeInfo accessibilityNodeInfo;
    /**
     * 返回回调接口
     */
    public OnPressListener onPressListener;

    public ControlService() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }


    public interface OnPressListener {
        /**
         * 点击回调接口
         */
        void onPress();
    }

    /**
     * 注册回调接口的方法，供外部调用
     *
     * @param onPressListener
     */
    public void setOnPressListenerr(OnPressListener onPressListener) {
        this.onPressListener = onPressListener;
    }


    /**
     * binder通信类
     */
    public class ControlBinder extends Binder {
        //Activity获取mUnityPlayer
        public void doSomething() {
            showControlWindow();
        }

        public void onBackPress() {
        }

        public ControlService getService() {
            return ControlService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mContext = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }        //window设置
        layoutParams.format = PixelFormat.RGBA_8888;
        //设置window出现位置
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.x = 300;
        layoutParams.y = 300;
        Display display = windowManager.getDefaultDisplay();
        Log.d(TAG, "width-display :" + display.getWidth());
        Log.d(TAG, "heigth-display :" + display.getHeight());
//        layoutParams.width = display.getWidth() / 10;
//        layoutParams.height = display.getHeight() / 8;
        layoutParams.width = 200;
        layoutParams.height = 200;
        showControlWindow();
    }


    /**
     * 显示悬浮球
     */
    public void showControlWindow() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        displayView = layoutInflater.inflate(R.layout.control_view, null);

        displayView.setOnTouchListener(new FloatingOnTouchListener());
        windowManager.addView(displayView, layoutParams);
    }

    /**
     * 监听悬浮窗移动,点击
     */
    private class FloatingOnTouchListener implements View.OnTouchListener {
        //滑动改变
        private int x;
        private int y;

        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();

        @SuppressLint("LongLogTag")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                //压下记录x，y
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();

                    startTime = System.currentTimeMillis();
                    break;
                //滑动更新
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    //更新munityplayer的父节点
                    windowManager.updateViewLayout(displayView, layoutParams);
                    break;
                //抬起手指
                case MotionEvent.ACTION_UP:
                    //当从点击到弹起小于半秒的时候,则判断为点击,如果超过则不响应点击事件
                    endTime = System.currentTimeMillis();
                    if ((endTime - startTime) > 0.1 * 1000L) {
                        Log.d(TAG, "onTouch: not a click");
                    } else {
                        Log.d(TAG, "onTouch: is a click");
                        //方法一无障碍
                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                        //方法二启动桌面应用
//                        Intent startMain = new Intent(Intent.ACTION_MAIN);
//                        startMain.addCategory(Intent.CATEGORY_HOME);
//                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(startMain);

                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
package com.itwwyi.org.customcamera.activity;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;

import com.itwwyi.org.customcamera.R;
import com.itwwyi.org.customcamera.camera.CameraInterface;
import com.itwwyi.org.customcamera.camera.preview.CameraSurfaceView;
import com.itwwyi.org.customcamera.ui.ControllerView;
import com.itwwyi.org.customcamera.util.DisplayUtil;

public class CameraActivity extends AppCompatActivity implements CameraInterface.CamOpenOverCallback {

    private static final String TAG = "vee";
    CameraSurfaceView surfaceView = null;
    ControllerView maskView = null;
    float previewRate = -1f;
    int DST_CENTER_RECT_WIDTH = 150; //单位是dip
    int DST_CENTER_RECT_HEIGHT = 350;//单位是dip
    Point rectPictureSize = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CameraInterface.getInstance().setApp(getApplication());
        setContentView(R.layout.activity_camera);
        initUI();
        initViewParams();

    }

    /**
     * 获取焦点的时候打开camera
     */
    @Override
    protected void onResume() {
        super.onResume();
        Thread openThread = new Thread() {
            @Override
            public void run() {
                SystemClock.sleep(50);
                CameraInterface.getInstance().doOpenCamera(CameraActivity.this);
            }
        };
        openThread.start();
    }

    private void initUI() {
        surfaceView = (CameraSurfaceView) findViewById(R.id.camera_surfaceview);
        maskView = (ControllerView) findViewById(R.id.controller);
        maskView.setOnCheckedListener(new ControllerView.OnCheckedListener() {
            @Override
            public void onComplete(View view) {
                if (rectPictureSize == null) {
                    rectPictureSize = createCenterPictureRect(DisplayUtil.dip2px(CameraActivity.this, DST_CENTER_RECT_WIDTH)
                            , DisplayUtil.dip2px(CameraActivity.this, DST_CENTER_RECT_HEIGHT));
                }
                CameraInterface.getInstance().doTakePicture(rectPictureSize.x, rectPictureSize.y);
            }
        });
    }

    private void initViewParams() {
        ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
        Point p = DisplayUtil.getScreenMetrics(this);
        params.width = p.x;
        params.height = p.y;
        Log.i(TAG, "screen: w = " + p.x + " y = " + p.y);
        previewRate = DisplayUtil.getScreenRate(this); //默认全屏的比例预览
        surfaceView.setLayoutParams(params);

    }

    @Override
    public void cameraHasOpened() {
        SurfaceHolder holder = surfaceView.getSurfaceHolder();
        CameraInterface.getInstance().doStartPreview(holder, previewRate);
        if (maskView != null) {
            Rect screenCenterRect = createCenterScreenRect(DisplayUtil.dip2px(this, DST_CENTER_RECT_WIDTH)
                    , DisplayUtil.dip2px(this, DST_CENTER_RECT_HEIGHT));
            maskView.setCenterRect(screenCenterRect);
        }
    }


    /**
     * 生成拍照后图片的中间矩形的宽度和高度
     *
     * @param w 屏幕上的矩形宽度，单位px
     * @param h 屏幕上的矩形高度，单位px
     * @return
     */
    private Point createCenterPictureRect(int w, int h) {

        int wScreen = DisplayUtil.getScreenMetrics(this).x;
        int hScreen = DisplayUtil.getScreenMetrics(this).y;
        int wSavePicture = CameraInterface.getInstance().doGetPictureSize().y; //因为图片旋转了，所以此处宽高换位
        int hSavePicture = CameraInterface.getInstance().doGetPictureSize().x; //因为图片旋转了，所以此处宽高换位
        float wRate = (float) (wSavePicture) / (float) (wScreen);
        float hRate = (float) (hSavePicture) / (float) (hScreen);
        float rate = (wRate <= hRate) ? wRate : hRate;//也可以按照最小比率计算

        int wRectPicture = (int) (w * wRate);
        int hRectPicture = (int) (h * hRate);
        return new Point(wRectPicture, hRectPicture);

    }

    /**
     * 生成屏幕中间的矩形
     *
     * @param w 目标矩形的宽度,单位px
     * @param h 目标矩形的高度,单位px
     * @return
     */
    private Rect createCenterScreenRect(int w, int h) {

        //x1 y1 左上角  x2 y2 右下角
        int x1 = DisplayUtil.getScreenMetrics(this).x / 2 - w / 2;
        int y1 = DisplayUtil.getScreenMetrics(this).y / 2 - h / 2 - DisplayUtil.dip2px(this, 80);
        int x2 = x1 + w;
        int y2 = y1 + h;
        return new Rect(x1, y1, x2, y2);
}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraInterface.getInstance().doStopCamera();
    }

}

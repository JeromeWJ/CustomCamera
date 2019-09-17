package com.wj.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;

/**
 * 拍照界面
 * * 5.0 版本以前的拍照
 * 照片存储目录示例：/storage/emulated/0/Android/data/com.wj.camera/files/IMG_20190917_161449.jpg
 *
 * @author wj
 * @date 2019-08-13
 */
public class CameraOneActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener {
    private static final String TAG = "CameraActivity";
    private SurfaceView mSvPreview;
    private ImageButton mIbBack;
    private ImageButton mIbCameraChange;
    private ImageButton mIbCameraFlash;
    private ImageButton mIbCameraCancel;
    private ImageButton mIbCamera;
    private ImageButton mIbCameraEdit;

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private CameraUtil cameraInstance;
    /**
     * 屏幕宽高
     */
    private int screenWidth;
    private int screenHeight;
    /**
     * 拍照id  1： 前摄像头  0：后摄像头
     */
    private int mCameraId = 0;
    /**
     * 闪光灯是否打开
     */
    private boolean isFlashOn;
    private File mFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mSvPreview = (SurfaceView) findViewById(R.id.sv_preview);
        mIbBack = (ImageButton) findViewById(R.id.ib_back);
        mIbCameraChange = (ImageButton) findViewById(R.id.ib_camera_change);
        mIbCameraFlash = (ImageButton) findViewById(R.id.ib_camera_flash);
        mIbCameraCancel = (ImageButton) findViewById(R.id.ib_camera_cancel);
        mIbCamera = (ImageButton) findViewById(R.id.ib_camera);
        mIbCameraEdit = (ImageButton) findViewById(R.id.ib_camera_edit);


        mHolder = mSvPreview.getHolder();
        mHolder.addCallback(this);
        cameraInstance = CameraUtil.getInstance();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        mIbCamera.setOnClickListener(this);
        mIbCameraFlash.setOnClickListener(this);
        mIbCameraChange.setOnClickListener(this);
        mIbBack.setOnClickListener(this);
        mIbCameraCancel.setOnClickListener(this);
        mIbCameraEdit.setOnClickListener(this);


        // C、解决图片旋转问题，此种方法，无论是横着拍还是竖着拍，保存的照片都是竖着拍的效果
        final IOrientationEventListener orientationEventListener = new IOrientationEventListener(this);
        mHolder.setKeepScreenOn(true);//屏幕常亮
        mHolder.addCallback(new SurfaceHolder.Callback() {
            //当SurfaceHolder被创建的时候回调
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                orientationEventListener.enable();
            }

            //当SurfaceHolder的尺寸发生变化的时候被回调
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            //当SurfaceHolder被销毁的时候回调
            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                orientationEventListener.disable();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = getCamera(mCameraId);
            if (mHolder != null) {
                startPreview(mCamera, mHolder);
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ib_camera) {
            if (mIbCamera.isSelected()) {
//                setPhotoResult(mFile.getAbsolutePath());
            } else {
                mIbCamera.setSelected(true);
                takePhoto();
                mIbCameraFlash.setVisibility(View.GONE);
                mIbCameraChange.setVisibility(View.GONE);
                mIbCameraCancel.setVisibility(View.VISIBLE);
                mIbCameraEdit.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.ib_camera_cancel) {
            releaseCamera();
            mCamera = getCamera(mCameraId);
            if (mHolder != null) {
                startPreview(mCamera, mHolder);
            }
            mIbCamera.setSelected(false);
            mIbCameraCancel.setVisibility(View.GONE);
            mIbCameraEdit.setVisibility(View.GONE);
            mIbCameraFlash.setVisibility(View.VISIBLE);
            mIbCameraChange.setVisibility(View.VISIBLE);
        } else if (id == R.id.ib_camera_edit) {
            //浏览编辑照片

        } else if (id == R.id.ib_camera_flash) {
            if (isFlashOn) {
                isFlashOn = false;
                mIbCameraFlash.setSelected(false);
                cameraInstance.turnLightOff(mCamera);
            } else {
                mIbCameraFlash.setSelected(true);
                isFlashOn = true;
                cameraInstance.turnLightOn(mCamera);
            }
        } else if (id == R.id.ib_camera_change) {
            switchCamera();
        } else if (id == R.id.ib_back) {
            finish();
        }
    }

    /**
     * 切换前后摄像头
     */
    public void switchCamera() {
        releaseCamera();
        mCameraId = (mCameraId + 1) % Camera.getNumberOfCameras();
        mCamera = getCamera(mCameraId);
        if (mHolder != null) {
            startPreview(mCamera, mHolder);
        }
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //针对拍摄的图片结果被旋转了90的解决方案有三种A,B,C
                mFile = CameraUtil.getInstance().getPhotoFilesDir(CameraOneActivity.this);
                CameraUtil.savePicture(data, mFile.getAbsolutePath());
                // B.通过ExifInterface对图片进行旋转，此种方法能保证预览界面和实际保存的图像一致
//                CameraUtil.setPictureDegreeZero(mFile.getAbsolutePath());
/*
                //A:下面的方法比较耗时，尤其图片较大的时候，不建议使用。
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap saveBitmap = cameraInstance.setTakePicktrueOrientation(mCameraId, bitmap);
                Log.e(TAG, "imgpath: ---  " + imgpath);
                CameraUtil.saveJPGE_After(getApplicationContext(), saveBitmap, imgpath, 100);
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                if (!saveBitmap.isRecycled()) {
                    saveBitmap.recycle();
                }*/
            }
        });

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview(mCamera, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        startPreview(mCamera, holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 预览相机
     */
    private void startPreview(Camera camera, SurfaceHolder holder) {
        try {
            setupCamera(camera);
            camera.setPreviewDisplay(holder);
            cameraInstance.setCameraDisplayOrientation(this, mCameraId, camera);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置surfaceView的尺寸 因为camera默认是横屏，所以取得支持尺寸也都是横屏的尺寸
     * 我们在startPreview方法里面把它矫正了过来，但是这里我们设置设置surfaceView的尺寸的时候要注意 previewSize.height<previewSize.width
     * previewSize.width才是surfaceView的高度
     * 一般相机都是屏幕的宽度 这里设置为屏幕宽度 高度自适应 你也可以设置自己想要的大小
     */
    private void setupCamera(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        //根据屏幕尺寸获取最佳 大小
        Camera.Size previewSize = cameraInstance.getPicPreviewSize(parameters.getSupportedPreviewSizes(),
                screenHeight, screenWidth);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        Camera.Size pictrueSize = cameraInstance.getPicPreviewSize(parameters.getSupportedPictureSizes(),
                screenHeight, screenWidth);
        parameters.setPictureSize(pictrueSize.width, pictrueSize.height);
        camera.setParameters(parameters);
    }

    /**
     * 获取Camera实例
     *
     * @return Camera
     */
    private Camera getCamera(int id) {
        Camera camera = null;
        try {
            camera = Camera.open(id);
        } catch (Exception e) {
            Log.e(TAG, "getCamera: " + e);
        }
        return camera;
    }


    public class IOrientationEventListener extends OrientationEventListener {

        public IOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (ORIENTATION_UNKNOWN == orientation) {
                return;
            }
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            orientation = (orientation + 45) / 90 * 90;
            int rotation = 0;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else {
                rotation = (info.orientation + orientation) % 360;
            }
            Log.e("TAG", "orientation: " + orientation);
            if (null != mCamera) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setRotation(rotation);
                mCamera.setParameters(parameters);
            }
        }
    }

}
package com.wj.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 拍照工具类
 */
public class CameraUtil {
    private static final String TAG = "CameraUtil";
    /**
     * 降序
     */
    private CameraDropSizeComparator dropSizeComparator = new CameraDropSizeComparator();
    /**
     * 升序
     */
    private CameraAscendSizeComparator ascendSizeComparator = new CameraAscendSizeComparator();
    private static CameraUtil instance = null;


    private CameraUtil() {

    }

    public static CameraUtil getInstance() {
        if (instance == null) {
            instance = new CameraUtil();
            return instance;
        } else {
            return instance;
        }
    }

    private int getRecorderRotation(int cameraId) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        return info.orientation;
    }

    /**
     * 获取所有支持的返回视频尺寸
     *
     * @param list      list
     * @param minHeight minHeight
     * @return Size
     */
    private Size getPropVideoSize(List<Size> list, int minHeight) {
        Collections.sort(list, ascendSizeComparator);

        int i = 0;
        for (Size s : list) {
            if ((s.height >= minHeight)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    /**
     * 保证预览方向正确
     *
     * @param activity activity
     * @param cameraId cameraId
     * @param camera   camera
     */
    public void setCameraDisplayOrientation(Activity activity,
                                            int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        //设置角度
        camera.setDisplayOrientation(result);
    }


    public Bitmap setTakePicktrueOrientation(int id, Bitmap bitmap) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(id, info);
        bitmap = rotaingImageView(id, info.orientation, bitmap);
        return bitmap;
    }

    /**
     * 把相机拍照返回照片转正
     *
     * @param angle 旋转角度
     * @return bitmap 图片
     */
    private Bitmap rotaingImageView(int id, int angle, Bitmap bitmap) {
        //矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        //加入翻转 把相机拍照返回照片转正
        if (id == 1) {
            matrix.postScale(-1, 1);
        }
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }



    /**
     * 修正图片的旋转角度，设置其不旋转。
     *
     * @param path
     */
    public static void setPictureDegreeZero(String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            // 例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取所有支持的预览尺寸
     *
     * @param list     list
     * @param minWidth minWidth
     * @return Size
     */
    private Size getPropPreviewSize(List<Size> list, int minWidth) {
        Collections.sort(list, ascendSizeComparator);

        int i = 0;
        for (Size s : list) {
            if ((s.width >= minWidth)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    /**
     * 获取所有支持的返回图片尺寸
     *
     * @param list     list
     * @param minWidth minWidth
     * @return Size
     */
    private Size getPropPictureSize(List<Size> list, int minWidth) {
        Collections.sort(list, ascendSizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            if ((s.width >= minWidth)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    /**
     * 获取所有支持的返回视频尺寸
     *
     * @param list      list
     * @param minHeight minHeight
     * @return Size
     */
    public Size getPropSizeForHeight(List<Size> list, int minHeight) {
        Collections.sort(list, ascendSizeComparator);
        int i = 0;
        for (Size s : list) {
            if ((s.height >= minHeight)) {
                Log.e(TAG, "getPropSizeForHeight: s.height=" + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = list.size();
        }
        return list.get(i);
    }

    /**
     * 根据 宽度和高度找到是否有相等的 尺寸  如果没有 就获取最小的 值
     *
     * @param list     list
     * @param th       高度
     * @param minWidth 宽度
     * @return size
     */
    public Size getPicPreviewSize(List<Size> list, int th, int minWidth) {
        Collections.sort(list, dropSizeComparator);

//        int i = 0;
//        for(int x=0;x<list.size();x++){
//            Size s = list.get(x);
//            // camera 中的宽度和高度 相反 因为测试板子原因 这里暂时 替换 && 为 ||
//            if((s.width == th) && (s.height == minWidth)){
//                i = x;
//                break;
//            }
//        }
        //如果没找到，就选最小的size 0
        return list.get(0);
    }

    public Size getPropPictureSize(List<Size> list, float th, int minWidth) {
        Collections.sort(list, ascendSizeComparator);

        int i = 0;
        for (Size s : list) {
            if ((s.width >= minWidth) && equalRate(s, th)) {
                Log.i(TAG, "PictureSize : w = " + s.width + "h = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;//如果没找到，就选最小的size
        }
        return list.get(i);
    }

    /**
     * 升序 按照高度
     */
    private class CameraAscendSizeComparatorForHeight implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            if (lhs.height == rhs.height) {
                return 0;
            } else if (lhs.height > rhs.height) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private boolean equalRate(Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        return Math.abs(r - rate) <= 0.03;
    }

    /**
     * 降序
     */
    private class CameraDropSizeComparator implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width < rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /**
     * 升序
     */
    private class CameraAscendSizeComparator implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /**
     * 打印支持的previewSizes
     *
     * @param params
     */
    private void printSupportPreviewSize(Camera.Parameters params) {
        List<Size> previewSizes = params.getSupportedPreviewSizes();
        for (int i = 0; i < previewSizes.size(); i++) {
            Size size = previewSizes.get(i);
        }

    }

    /**
     * 打印支持的pictureSizes
     *
     * @param params
     */
    private void printSupportPictureSize(Camera.Parameters params) {
        List<Size> pictureSizes = params.getSupportedPictureSizes();
        for (int i = 0; i < pictureSizes.size(); i++) {
            Size size = pictureSizes.get(i);
        }
    }

    /**
     * 打印支持的聚焦模式
     *
     * @param params params
     */
    private void printSupportFocusMode(Camera.Parameters params) {
        List<String> focusModes = params.getSupportedFocusModes();
        for (String mode : focusModes) {
            Log.e(TAG, "printSupportFocusMode: " + mode);
        }
    }

    /**
     * 打开闪关灯
     *
     * @param mCamera mCamera
     */
    public void turnLightOn(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null) {
            return;
        }
        String flashMode = parameters.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            }
        }
    }

    /**
     * 自动模式闪光灯
     *
     * @param mCamera mCamera
     */
    public void turnLightAuto(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null) {
            return;
        }
        String flashMode = parameters.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_AUTO.equals(flashMode)) {
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            }
        }
    }

    /**
     * 关闭闪光灯
     *
     * @param mCamera mCamera
     */
    public void turnLightOff(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        String flashMode = parameters.getFlashMode();
        if (flashModes == null) {
            return;
        }
        if (!Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
            }
        }
    }


    /**
     * 保存图片为PNG
     *
     * @param bitmap
     * @param name
     */
    public static void savePNG_After(Bitmap bitmap, String name) {
        File file = new File(name);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存图片为JPEG
     *
     * @param bitmap
     * @param path
     */
    public static boolean saveJPGE_After(Bitmap bitmap, String path, int quality) {
        File file = new File(path);
        makeDir(file);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 保存图片为JPEG
     *
     * @param bitmap
     * @param path
     */
    public static void saveJPGE_After(Context context, Bitmap bitmap, String path, int quality) {
        File file = new File(path);
        makeDir(file);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存图片为PNG
     *
     * @param bitmap
     * @param path
     */
    public static void saveJPGE_After_PNG(Context context, Bitmap bitmap, String path, int quality) {
        File file = new File(path);
        makeDir(file);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, quality, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 保存图片
     *
     * @param data
     * @param path
     */
    public static void savePicture(byte[] data,String path) {
        File file = new File(path);
        makeDir(file);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void makeDir(File file) {
        File tempPath = new File(file.getParent());
        if (!tempPath.exists()) {
            tempPath.mkdirs();
        }
    }


    public File getPhotoFilesDir(Activity activity) {
        File mediaStorageDir = new File(activity.getExternalFilesDir(null).getAbsolutePath());
        File file = new File(this.getFilePath(mediaStorageDir));
        File tempPath = new File(file.getParent());
        if (!tempPath.exists()) {
            tempPath.mkdirs();
        }
        return file;
    }

    private String getFilePath(File mediaStorageDir) {
        String timeStamp = (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date());
        String filePath = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
        return filePath;
    }
}

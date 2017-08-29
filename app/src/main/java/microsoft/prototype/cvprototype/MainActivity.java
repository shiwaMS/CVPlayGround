package microsoft.prototype.cvprototype;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import microsoft.prototype.cvprototype.app.FileUtils;
import microsoft.prototype.cvprototype.app.NativeOpenCVClass;
import microsoft.prototype.cvprototype.app.PermissionsManager;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static microsoft.prototype.cvprototype.app.FileUtils.EYEGLASSES_FILE;
import static microsoft.prototype.cvprototype.app.FileUtils.FACE_FILE;
import static microsoft.prototype.cvprototype.app.PermissionsManager.Permission.CAMERA;
import static microsoft.prototype.cvprototype.app.PermissionsManager.Permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    JavaCameraView javaCameraView;
    Mat mat, imageGray;

    private String eyeglassesFilePath;
    private String faceFilePath;

    private final Object matLock = new Object();
    private long frameCount;
    private MatOfRect faces;

    private HandlerThread detectThread;
    private Handler detectHandler;

    static {
        System.loadLibrary("MyOpenCVLibs");
    }

    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    if (javaCameraView != null) {
                        Log.d(TAG, "javaCameraView enabled");
                        javaCameraView.enableView();
                    }
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        this.javaCameraView.setVisibility(SurfaceView.VISIBLE);
        this.javaCameraView.setCvCameraViewListener(this);

        this.eyeglassesFilePath = FileUtils.getFilePath(this, EYEGLASSES_FILE);
        this.faceFilePath = FileUtils.getFilePath(this, FACE_FILE);
        this.faces = new MatOfRect();

        PermissionsManager.INSTANCE.requestPermissionsIfNotGranted(this, CAMERA, WRITE_EXTERNAL_STORAGE);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_13, this, this.baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV loaded");
            this.baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this.javaCameraView != null) {
            this.javaCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.mat = new Mat(height, width, CvType.CV_8UC4);
        this.imageGray = new Mat(height, width, CvType.CV_8UC1);

        this.detectThread = new HandlerThread("DetectThread");
        this.detectThread.start();
        this.detectHandler = new Handler(this.detectThread.getLooper());

        this.frameCount = 0;
        this.faces = new MatOfRect();
    }

    @Override
    public void onCameraViewStopped() {
        this.mat.release();
        this.imageGray.release();
        this.faces.release();

        this.detectThread.quitSafely();
        try {
            this.detectThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
//        NativeOpenCVClass.convertGray(this.mat.getNativeObjAddr(), this.imageGray.getNativeObjAddr());
//        Imgproc.cvtColor(this.mat, imageGray, Imgproc.COLOR_RGBA2GRAY);
//
        this.frameCount++;
        this.mat = inputFrame;

        detectHandler.post(() -> {
            long startTime = System.currentTimeMillis();
            NativeOpenCVClass.faceDetection(this.mat.getNativeObjAddr(), faces.getNativeObjAddr(), this.faceFilePath, this.eyeglassesFilePath);
            long endTime = System.currentTimeMillis();
            Log.i(TAG, String.valueOf((endTime - startTime) / 1000f) + " sec");
        });

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(this.mat, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return this.mat;
    }
}

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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import microsoft.prototype.cvprototype.app.NativeOpenCVClass;
import microsoft.prototype.cvprototype.app.PermissionsManager;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static microsoft.prototype.cvprototype.app.PermissionsManager.Permission.CAMERA;
import static microsoft.prototype.cvprototype.app.PermissionsManager.Permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String EYEGLASSES_FILE = "haarcascade_eye_tree_eyeglasses.xml";
    private static final String FACE_FILE = "haarcascade_frontalface_alt.xml";

    JavaCameraView javaCameraView;
    Mat mat, imageGray;

    private String eyeglassesFilePath;
    private String faceFilePath;

    private final Object matLock = new Object();

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

        this.eyeglassesFilePath = this.getFilePath(EYEGLASSES_FILE);
        this.faceFilePath = this.getFilePath(FACE_FILE);

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
    }

    @Override
    public void onCameraViewStopped() {
        this.mat.release();
        this.imageGray.release();

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
//        this.detectHandler.post(() -> {
            this.mat = inputFrame;
            NativeOpenCVClass.faceDetection(this.mat.getNativeObjAddr(), this.faceFilePath, this.eyeglassesFilePath);
//        });

        return this.mat;
    }


    private String getFilePath(String targetFile) {
        String filePath = "";
        Log.d(TAG, "targetFile: " + targetFile);

        try {
            String rootDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Temp";
            File rootDir = new File(rootDirPath);
            if (!rootDir.exists()) {
                rootDir.mkdir();
                Log.d(TAG, "rootDir created");
            }

            Log.d(TAG, "File rootDir getAbsolutePath: " + rootDirPath);

            File file = new File(rootDir, targetFile);
            if (!file.exists()) {
                file.createNewFile();
                Log.d(TAG, "File created");
            }

            InputStream is = this.getAssets().open(targetFile);
            OutputStream os = new FileOutputStream(file, false);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = is.read(bytes)) != -1) {
                os.write(bytes, 0, read);
            }

            filePath = file.getAbsolutePath();
            Log.d(TAG, "Write file finished");
            Log.i(TAG, "File getAbsolutePath: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;
    }
}

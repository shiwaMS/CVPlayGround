package microsoft.prototype.cvprototype;

import android.content.Context;
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
    }

    @Override
    public void onCameraViewStopped() {
        this.mat.release();
        this.imageGray.release();
    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        this.mat = inputFrame;

//        NativeOpenCVClass.convertGray(this.mat.getNativeObjAddr(), this.imageGray.getNativeObjAddr());
//        Imgproc.cvtColor(this.mat, imageGray, Imgproc.COLOR_RGBA2GRAY);
        NativeOpenCVClass.faceDetection(this.mat.getNativeObjAddr(), this.faceFilePath, this.eyeglassesFilePath);

        return this.mat;
    }


    private String getFilePath(String targetFile) {
        String filePath = "";
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            File rootDir = getExternalFilesDir()
            inputStream = this.getAssets().open(targetFile);

            File file = new File(getFilesDir(), targetFile);
            if (!file.exists()) {
                file.createNewFile();
                Log.d(TAG, "File created");
            }

            fileOutputStream = new FileOutputStream(file, false);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, read);
            }

            Log.d(TAG, "Write file finished");

            filePath = file.getAbsolutePath();
            Log.i(TAG, "File path: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return filePath;
    }
}

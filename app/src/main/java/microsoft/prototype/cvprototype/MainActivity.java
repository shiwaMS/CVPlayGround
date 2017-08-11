package microsoft.prototype.cvprototype;

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

import microsoft.prototype.cvprototype.app.NativeOpenCVClass;
import microsoft.prototype.cvprototype.app.PermissionsManager;

import static microsoft.prototype.cvprototype.app.PermissionsManager.Permission.CAMERA;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    JavaCameraView javaCameraView;
    Mat mat, imageGray;

    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    if(javaCameraView != null){
                        javaCameraView.enableView();
                    }
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        }else {
            Log.d(TAG, "OpenCV loaded");
        }

        System.loadLibrary("MyOpenCVLibs");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        this.javaCameraView.setVisibility(SurfaceView.VISIBLE);
        this.javaCameraView.setCvCameraViewListener(this);

        PermissionsManager.INSTANCE.requestPermissionsIfNotGranted(this, CAMERA);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
            this.baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else {
            Log.d(TAG, "OpenCV loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_13, this, this.baseLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(this.javaCameraView != null){
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(this.javaCameraView != null){
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
        NativeOpenCVClass.convertGray(this.mat.getNativeObjAddr(), this.imageGray.getNativeObjAddr());
        return this.imageGray;
    }
}

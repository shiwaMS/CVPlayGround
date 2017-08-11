package microsoft.prototype.cvprototype.app;

public class NativeOpenCVClass {
    public native static int convertGray(long matAddrRgba, long matGray);
    public native static void faceDetection(long addrRgba, String faceFileDir, String eyeglassFileDir);
}

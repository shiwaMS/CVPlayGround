package microsoft.prototype.cvprototype.app;

public class NativeOpenCVClass {
    public native static int convertGray(long matAddrRgba, long matGray);
    public native static void faceDetection(long addrRgba, long addrFaces, String faceFileDir, String eyeglassFileDir);
}

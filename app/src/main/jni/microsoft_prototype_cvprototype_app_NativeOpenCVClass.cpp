#include <microsoft_prototype_cvprototype_app_NativeOpenCVClass.h>

JNIEXPORT jint JNICALL Java_microsoft_prototype_cvprototype_app_NativeOpenCVClass_convertGray
        (JNIEnv *env, jclass, jlong addrRgba, jlong addrGray) {

    Mat &mRgba = *(Mat *) addrRgba;
    Mat &mGray = *(Mat *) addrGray;

    int conv;
    jint result;

    conv = toGray(mRgba, mGray);
    result = (jint) conv;

    return result;
}

int toGray(Mat img, Mat &gray) {
    cvtColor(img, gray, CV_RGBA2GRAY);

    if (gray.rows == img.rows && gray.cols == img.cols) {
        return 1;
    }

    return 0;
}


/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <stdio.h>
#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;
/* Header for class microsoft_prototype_cvprototype_app_NativeOpenCVClass */

#ifndef _Included_microsoft_prototype_cvprototype_app_NativeOpenCVClass
#define _Included_microsoft_prototype_cvprototype_app_NativeOpenCVClass
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     microsoft_prototype_cvprototype_app_NativeOpenCVClass
 * Method:    convertGray
 * Signature: (JJ)I
 */
int toGray(Mat img, Mat &gray);

JNIEXPORT jint JNICALL Java_microsoft_prototype_cvprototype_app_NativeOpenCVClass_convertGray
        (JNIEnv *, jclass, jlong, jlong);

#ifdef __cplusplus
}
#endif
#endif

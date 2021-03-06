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

//JNIEXPORT void JNICALL
//Java_microsoft_prototype_cvprototype_app_NativeOpenCVClass_faceDetection
//        (JNIEnv *env, jclass type, jlong addrRgba){
//    Mat& frame = *(Mat*) addrRgba;
//
//    detect(frame);
//}

JNIEXPORT void JNICALL
Java_microsoft_prototype_cvprototype_app_NativeOpenCVClass_faceDetection(JNIEnv *env, jclass type,
                                                                         jlong addrRgba,
                                                                         jlong addrFaces,
                                                                         jstring faceFileDir_,
                                                                         jstring eyeglassFileDir_) {
    const char *faceFileDir = env->GetStringUTFChars(faceFileDir_, 0);
    const char *eyeglassFileDir = env->GetStringUTFChars(eyeglassFileDir_, 0);

    Mat &frame = *(Mat *) addrRgba;
    Mat &faces = *(Mat *) addrFaces;

    detect(frame, faces, faceFileDir, eyeglassFileDir);

    env->ReleaseStringUTFChars(faceFileDir_, faceFileDir);
    env->ReleaseStringUTFChars(eyeglassFileDir_, eyeglassFileDir);
}

void detect(Mat &frame, Mat &faces, const char *face_name, const char *eyes_name) {
    String face_cascade_name(face_name);
    String eyes_cascade_name(eyes_name);
//    String face_cascade_name = "/storage/emulated/0/Temp/haarcascade_frontalface_alt.xml";
//    String eyes_cascade_name = "/storage/emulated/0/Temp/haarcascade_eye_tree_eyeglasses.xml";
    CascadeClassifier face_cascade;
    CascadeClassifier eyes_cascade;

    // Load the cascades
    if (!face_cascade.load(face_cascade_name)) {
        printf("--(!)Error loading\n");
        return;
    };
//    if (!eyes_cascade.load(eyes_cascade_name)) {
//        printf("--(!)Error loading\n");
//        return;
//    };

    Mat frame_gray;
    std::vector<Rect> facesVec;

    cvtColor(frame, frame_gray, CV_BGR2GRAY);
    equalizeHist(frame_gray, frame_gray);


    clock_t startTime = clock();
    //-- Detect faces
    face_cascade.detectMultiScale(frame_gray, facesVec, 1.2, 3, 0 | CV_HAAR_SCALE_IMAGE, Size(20, 20));
    faces = Mat(facesVec, true);
    double secondsPassed = (clock() - startTime) / (double) CLOCKS_PER_SEC;
    LOGD("MainActivity JNI detect: %lf", secondsPassed);

//    for (size_t i = 0; i < faces.size(); i++) {
//        Point center(faces[i].x + faces[i].width * 0.5, faces[i].y + faces[i].height * 0.5);
//        ellipse(frame, center, Size(faces[i].width * 0.5, faces[i].height * 0.5), 0, 0, 360,
//                Scalar(255, 0, 255), 4, 8, 0);

//        Mat faceROI = frame_gray(faces[i]);
//        std::vector<Rect> eyes;

//        //-- In each face, detect eyes
//        eyes_cascade.detectMultiScale(faceROI, eyes, 1.1, 2, 0 | CV_HAAR_SCALE_IMAGE, Size(30, 30));
//
//        for (size_t j = 0; j < eyes.size(); j++) {
//            Point center(faces[i].x + eyes[j].x + eyes[j].width * 0.5,
//                         faces[i].y + eyes[j].y + eyes[j].height * 0.5);
//            int radius = cvRound((eyes[j].width + eyes[j].height) * 0.25);
//            circle(frame, center, radius, Scalar(255, 0, 0), 4, 8, 0);
//        }
//    }
}


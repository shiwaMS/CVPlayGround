	LOCAL_PATH := $(call my-dir)

	include $(CLEAR_VARS)

	APP_ABI:=armeabi-v7a

	#opencv
	OPENCVROOT:= $(LOCAL_PATH)
	OPENCV_CAMERA_MODULES:=on
	OPENCV_INSTALL_MODULES:=on
	OPENCV_LIB_TYPE:=SHARED
	include ${LOCAL_PATH}/openCV/OpenCV.mk

	LOCAL_SRC_FILES := microsoft_prototype_cvprototype_app_NativeOpenCVClass.cpp

	LOCAL_LDLIBS += -llog
	LOCAL_MODULE := MyOpenCVLibs

	include $(BUILD_SHARED_LIBRARY)
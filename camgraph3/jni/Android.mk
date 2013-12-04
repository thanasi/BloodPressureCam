LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include ../OpenCV/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := imagemanipulations
LOCAL_LDLIBS    += -lm -llog -landroid
LOCAL_STATIC_LIBRARIES := android_native_app_glue

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/native_app_glue)
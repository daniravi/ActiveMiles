LOCAL_PATH := $(call my-dir)
ROOT_PATH := $(LOCAL_PATH)

include $(call all-subdir-makefiles)
include $(CLEAR_VARS)

LOCAL_PATH = $(ROOT_PATH)
LOCAL_CFLAGS := -Wall -Wextra

LOCAL_MODULE := torchdemo


LOCAL_C_INCLUDES += ../fftw3/include
LOCAL_C_INCLUDES += ../install/include

LOCAL_SRC_FILES := torchandroid.cpp torchdemo.cpp android_fopen.c

LOCAL_LDLIBS := -llog -landroid -L ../install/lib -L ../install/libs/armeabi-v7a  -lluaT -lluajit -lTH  -lTHNN -lnn  -ltorch -lnnx -limage -ltorchandroid -lluaT -lluajit -lTH -lTHNN -lnn  -ltorch -lnnx -limage -ltorchandroid
LOCAL_STATIC_LIBRARIES := fftw3

include $(BUILD_SHARED_LIBRARY)

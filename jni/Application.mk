# Build only ARMv7-A machine code.
APP_ABI := armeabi-v7a
APP_STL :=gnustl_static
LOCAL_ARM_NEON := true
LOCAL_CFLAGS += -fopenmp
LOCAL_LDFLAGS += -fopenmp
APP_OPTIM  := release
APP_PLATFORM := android-21
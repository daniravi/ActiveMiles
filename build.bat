@echo on

@set BASHPATH="C:\NVPACK\cygwin\bin\bash"
@set PROJECTDIR="/cygdrive/c/NVPACK/OpenCV-2.4.5-Tegra-sdk-r2/samples/ActiveMiles/ActiveMilesPro"
@set NDKDIR="/cygdrive/c/NVPACK2/android-sdk-windows/ndk-bundle/build/ndk-build"

%BASHPATH% --login -c "cd %PROJECTDIR% && %NDKDIR% -B


cd "%~dp0"
cp -r ../install/libs/armeabi-v7a/*.so libs/armeabi-v7a/


pause

@echo "/cygdrive/c/NVPACK2/android-ndk-r10d/ndk-build"
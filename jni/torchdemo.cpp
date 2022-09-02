/*
 * Copyright (C) 2013 e-lab Purdue
 *
 */
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include "torchandroid.h"
#include <assert.h>
#include <fftw3.h>
#include <vector>
#include <math.h>
#include <string>
#include <iostream>
//#include <time.h>
#include <sys/time.h>

#define PI 3.14
#define SignalLength 200
#define NTIMEPOINTS 5

void STFT(std::vector<float> *signal, int signalLength, int windowSize,
		int hopSize);

// Create a hamming window of windowLength samples in buffer
void hamming(int windowLength, float *buffer) {

	for (int i = 0; i < windowLength; i++) {

		buffer[i] = 0.54
				- (0.46 * cos(2 * PI * (i / ((windowLength - 1) * 1.0))));

	}

}

void STFT(std::vector<float> *signal, int signalLength, int windowSize,
		int hopSize) {

	fftw_complex *data, *fft_result, *ifft_result;
	fftw_plan plan_forward;
	int i;

	data = (fftw_complex*) fftw_malloc(sizeof(fftw_complex) * windowSize);
	fft_result = (fftw_complex*) fftw_malloc(sizeof(fftw_complex) * windowSize);
	ifft_result = (fftw_complex*) fftw_malloc(
			sizeof(fftw_complex) * windowSize);

	plan_forward = fftw_plan_dft_1d(windowSize, data, fft_result, FFTW_FORWARD,
	FFTW_ESTIMATE);

	// Create a hamming window of appropriate length
	float window[windowSize];
	hamming(windowSize, window);

	int chunkPosition = 0;

	int readIndex;

	// Should we stop reading in chunks?
	int bStop = 0;

	int numChunks = 0;

	// Process each chunk of the signal
	while (chunkPosition < signalLength && !bStop) {

		// Copy the chunk into our buffer
		for (i = 0; i < windowSize; i++) {

			readIndex = chunkPosition + i;

			if (readIndex < signalLength) {

				// Note the windowing!
				data[i][0] = (*signal)[readIndex] * window[i];
				data[i][1] = 0.0;

			} else {

				// we have read beyond the signal, so zero-pad it!

				data[i][0] = 0.0;
				data[i][1] = 0.0;

				bStop = 1;

			}
		}

		// Perform the FFT on our chunk
		fftw_execute(plan_forward);

		chunkPosition += hopSize;
		numChunks++;

	} // Excuse the formatting, the while ends here.

	fftw_destroy_plan(plan_forward);

	fftw_free(data);
	fftw_free(fft_result);
	fftw_free(ifft_result);

}

void MySTDT(std::vector<float> * signal, int size, int nPinTime) {
	for (int i = 0; i < nPinTime; i++) {
		STFT(signal, size, size / pow(2, i), size / pow(2, i));
	}
}

extern "C" {

JNIEXPORT long JNICALL
Java_org_imperial_activemilespro_service_ActivityDetectorBluetooth_initTorch(JNIEnv *env, jobject thiz, jobject assetManager, jstring nativeLibraryDir_)
{
// get native asset manager. This allows access to files stored in the assets folder
	AAssetManager* manager = AAssetManager_fromJava(env, assetManager);
	assert( NULL != manager);
	const char *nativeLibraryDir = env->GetStringUTFChars(nativeLibraryDir_, 0);

	lua_State *L = NULL;
	L = inittorch(manager, nativeLibraryDir);// create a lua_State

// load and run file
	char file[] = "mainBluetooth.lua";
	int ret;
	long size = android_asset_get_size(file);
	if (size != -1) {
		char *filebytes = android_asset_get_bytes(file);
		ret = luaL_dobuffer(L, filebytes, size, "main");
		if (ret == 1) {
			D("Torch Error doing resource: \n");
			D(lua_tostring(L,-1));
		} else {
			D("Torch script ran succesfully.");
		}
	}

	return (long) L;
}


JNIEXPORT long JNICALL
Java_org_imperial_activemilespro_service_ActivityDetectorInertial_initTorch(JNIEnv *env, jobject thiz, jobject assetManager, jstring nativeLibraryDir_)
{
// get native asset manager. This allows access to files stored in the assets folder
	AAssetManager* manager = AAssetManager_fromJava(env, assetManager);
	assert( NULL != manager);
	const char *nativeLibraryDir = env->GetStringUTFChars(nativeLibraryDir_, 0);

	lua_State *L = NULL;
	L = inittorch(manager, nativeLibraryDir);// create a lua_State

// load and run file
	char file[] = "mainInertial.lua";
	int ret;
	long size = android_asset_get_size(file);
	if (size != -1) {
		char *filebytes = android_asset_get_bytes(file);
		ret = luaL_dobuffer(L, filebytes, size, "main");
		if (ret == 1) {
			D("Torch Error doing resource: \n");
			D(lua_tostring(L,-1));
		} else {
			D("Torch script ran succesfully.");
		}
	}

	return (long) L;
}
JNIEXPORT jstring JNICALL
Java_org_imperial_activemilespro_service_ActivityDetectorBase_FFT(JNIEnv *env, jobject thiz)
{

	char msg[50]="";
	long int diffTimeMS;
	struct timeval startTime;
	struct timeval endTime;

	std::vector <float> *accelx = new std::vector<float>();
	std::vector <float> *accely = new std::vector<float>();
	std::vector <float> *accelz = new std::vector<float>();
	std::vector <float> *gyrox = new std::vector<float>();
	std::vector <float> *gyroy = new std::vector<float>();
	std::vector <float> *gyroz = new std::vector<float>();
	for (int i = 0; i< SignalLength; i++) {
		accelx->push_back(0.0f);
		accely->push_back(0.0f);
		accelz->push_back(0.0f);
		gyrox->push_back(0.0f);
		gyroy->push_back(0.0f);
		gyroz->push_back(0.0f);
	}

	//time_t startTime = time(0);
	gettimeofday(&startTime, NULL);

	MySTDT(accelx, SignalLength, NTIMEPOINTS);
	MySTDT(accely, SignalLength, NTIMEPOINTS);
	MySTDT(accelz, SignalLength, NTIMEPOINTS);
	MySTDT(gyrox, SignalLength, NTIMEPOINTS);
	MySTDT(gyroy, SignalLength, NTIMEPOINTS);
	MySTDT(gyroz, SignalLength, NTIMEPOINTS);

	//time_t endTime = time(0);
	gettimeofday(&endTime, NULL);

	//diffTimeSeconds = difftime(endTime, startTime);
	diffTimeMS = (endTime.tv_sec - startTime.tv_sec) * 1000 + (endTime.tv_usec - startTime.tv_usec) / 1000;

	sprintf(msg, "%ld", diffTimeMS);
	return env->NewStringUTF(msg);
}

JNIEXPORT jstring JNICALL
Java_org_imperial_activemilespro_service_ActivityDetectorBase_callTorch(JNIEnv *env, jobject thiz, jlong torchStateLocation,jdoubleArray dataFeat, jint size)
{
	lua_State *L = (lua_State*) torchStateLocation;
	char msg[1000]="";


	THDoubleTensor *testTensor = THDoubleTensor_newWithSize1d(size); //Initialize 1D tensor.
	jdouble *testTensor_data = (env)->GetDoubleArrayElements(dataFeat,0);//Get pointer to java byte array region
	jdouble *poutPixels = THDoubleTensor_data(testTensor);//Torch tensor type to int
	for(int i = 0; i < size; i++)
	{
		poutPixels[i] = testTensor_data[i];
	}

	lua_getglobal(L,"getDetections");
	lua_getglobal(L,"network");
	lua_getglobal(L,"mean");
	lua_getglobal(L,"std");
	luaT_pushudata(L,testTensor,"torch.DoubleTensor"); //Push tensor to lua stack

	if(lua_pcall(L,4,2,0) != 0) //Call function. Print error if call not successful
	__android_log_print(ANDROID_LOG_INFO, "Torchandroid", "Error running function: %s",lua_tostring(L, -1));

	else {

		sprintf(msg,"%s,",lua_tostring(L,-1));
		//sprintf(msg, "%f--", netProfiler);
		lua_pop(L,1);
		sprintf(msg+ + strlen(msg),"%s",lua_tostring(L,-1));
		lua_pop(L,1);
	}
	return env->NewStringUTF(msg);
}


JNIEXPORT void JNICALL
Java_org_imperial_activemilespro_service_ActivityDetectorBase_destroyTorch(JNIEnv *env, jobject thiz,jlong torchStateLocation)
{
	lua_State *L = (lua_State*) torchStateLocation;
	lua_close(L); //Close lua state.
}

}

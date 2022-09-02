package org.imperial.activemilespro.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.database.ImageTable;
import org.imperial.activemilespro.database.PerformanceContentProvider;
import org.imperial.activemilespro.diary.DiaryActivity;
import org.imperial.activemilespro.interface_utility.UtilsCalendar;
import org.imperial.activemilespro.service.Buzzer;
import org.imperial.activemilespro.service.SensorDataServiceInertial;
import org.imperial.activemilespro.service.SensorDataServiceBase;
import org.imperial.activemilespro.service.SensorDataServiceBluetooth;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class CameraView extends Activity implements OnTouchListener {
    public static final String ImageFileLoc = Environment.getExternalStorageDirectory() + "/ActiveMiles/";
    public static final String IconFileLoc = Environment.getExternalStorageDirectory() + "/ActiveMiles/Icon/";
    private MediaPlayer mp;
    private Buzzer sb;
    private TextView button_Shot;
    private SensorDataServiceBase remoteService = null;                                                            //
    private Location currentLocation;
    private ImageView PhotoPreview;
    private final static String TAG = "CameraView";
    private RelativeLayout bottonBar;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private int mState = STATE_PREVIEW;
    private final Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private String mCameraId;
    private AutoFitTextureView mTextureView;
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private Size mPreviewSize;
    private int idCamera = 0;
    private boolean isBinded = false;
    private Connection myConnection;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 201;


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setAlpha(.5f);
            if (this.getResources().getResourceEntryName(v.getId()).equalsIgnoreCase("Button_Shot"))
                startPlay1();
            else
                startPlay2();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            v.setAlpha(1f);
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File file;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.camera);

        button_Shot = (TextView) findViewById(R.id.Button_Shot);

        button_Shot.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                takePicture();
            }

        });

        file = new File(ImageFileLoc);
        if (!file.exists())
            file.mkdir();
        file = new File(IconFileLoc);
        if (!file.exists())
            file.mkdir();
        sb = new Buzzer(this);

        if (ActiveMilesGUI.DeviceType == 2) {
            isBinded = getApplicationContext().bindService(new Intent(CameraView.this, SensorDataServiceBluetooth.class), mConnection, Context.BIND_AUTO_CREATE);

        } else
            isBinded = getApplicationContext().bindService(new Intent(CameraView.this, SensorDataServiceInertial.class), mConnection, Context.BIND_AUTO_CREATE);

        ImageView rb1 = (ImageView) this.findViewById(R.id.Button_BarCode);
        rb1.setOnClickListener(new RadioButton.OnClickListener() {
            @Override
            public void onClick(View vee) {
                OpenBarCoder(null);
            }
        });

        ImageView rb2 = (ImageView) this.findViewById(R.id.Button_QRCode);
        rb2.setOnClickListener(new RadioButton.OnClickListener() {
            @Override
            public void onClick(View vee) {
                OpenQrCoder(null);
            }
        });

        ImageView rb3 = (ImageView) this.findViewById(R.id.Button_Photo);

        TextView switchButton = (TextView) this.findViewById(R.id.Button_SwithCamera);
        switchButton.setOnTouchListener(this);

        rb1.setOnTouchListener(this);
        rb2.setOnTouchListener(this);
        rb3.setOnTouchListener(this);
        button_Shot.setOnTouchListener(this);

        PhotoPreview = (ImageView) this.findViewById(R.id.PhotoPreview);
        PhotoPreview.setOnTouchListener(this);

        mTextureView = (AutoFitTextureView) findViewById(R.id.textureView1);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        bottonBar = (RelativeLayout) this.findViewById(R.id.bottonBar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

    }

    public void openAlbum(View ignored) {
        Intent intent = new Intent(CameraView.this, DiaryActivity.class);
        intent.putExtra("data", System.currentTimeMillis());
        startActivity(intent);
    }

    private void EnableButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                button_Shot.setEnabled(true);

            }
        });

    }

    private void DisabelButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                button_Shot.setEnabled(false);

            }
        });

    }

    public void onDestroy() {
        if (myConnection != null)
            myConnection.cancel(true);
        if (isBinded) {
            getApplicationContext().unbindService(mConnection);
            mConnection.onServiceDisconnected(null);
        }
        super.onDestroy();

    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {

            remoteService = ((SensorDataServiceBase.LocalBinder) rawBinder).getService();
            currentLocation = remoteService.getLocationTracker().getCurrentLocation();
            remoteService.getLocationTracker().setProviderPrecision(remoteService.getLocationTracker().MaxPrecision);

        }

        public void onServiceDisconnected(ComponentName classname) {

            Uri uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_SETTING);

            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            int levelOfPrecision = 0;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    levelOfPrecision = cursor.getInt(2);
                }
                cursor.close();
            }
            if (remoteService != null)
                remoteService.getLocationTracker().setProviderPrecision(levelOfPrecision);

        }
    };

    public void OpenQrCoder(View ignored2) {

        try {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setCaptureActivity(CaptureActivity.class);
            integrator.setOrientationLocked(true);
            integrator.setPrompt("Scan a barcode");
            integrator.setCameraId(0);  // Use a specific camera of the device
            integrator.setBeepEnabled(true);
            integrator.addExtra("SCAN_MODE", "QR_CODE_TYPES");
            integrator.initiateScan();

        } catch (ActivityNotFoundException ignored) {

        }
    }

    public void OpenBarCoder(View ignored2) {

        try {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
            integrator.setCaptureActivity(CaptureActivity.class);
            integrator.setOrientationLocked(true);
            integrator.setPrompt("Scan a barcode");
            integrator.setCameraId(0);  // Use a specific camera of the device
            integrator.setBeepEnabled(true);
            integrator.addExtra("SCAN_MODE", "ONE_D_CODE_TYPES");
            integrator.initiateScan();

        } catch (ActivityNotFoundException ignored) {

        }
    }

    private void startPlay1() {
        mp = MediaPlayer.create(this, R.raw.camera_click);
        try {
            mp.prepare();
        } catch (IllegalStateException | IOException ignored) {
        }
        mp.start();
    }

    private void startPlay2() {
        mp = MediaPlayer.create(this, R.raw.like_main);
        try {
            mp.prepare();
        } catch (IllegalStateException | IOException ignored) {
        }
        mp.start();
    }

    private void addInDatabase(long name) {
        sb.buzz();

        ContentValues values = new ContentValues();

        Log.d(TAG, currentLocation.toString());
        values.put(ImageTable.COLUMN_LAT, currentLocation.getLatitude());
        values.put(ImageTable.COLUMN_LNG, currentLocation.getLongitude());
        values.put(ImageTable.COLUMN_NAME, name);
        values.put(ImageTable.COLUMN_TIME_STAMP, UtilsCalendar.timeToStringDate(name).substring(0, 8));
        Uri uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_IMAGE);
        getContentResolver().insert(uri, values);
    }

    private Bitmap saveIcon(long name, File OriginalImage) throws IOException {
        Bitmap bmp = BitmapFactory.decodeFile(OriginalImage.getAbsolutePath(), new BitmapFactory.Options());
        File file = new File(IconFileLoc, Long.toString(name) + ".jpg");
        int sizeIcon = 150;
        bmp = Bitmap.createScaledBitmap(bmp, sizeIcon, sizeIcon, true);
        OutputStream fOutputStream = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);
        fOutputStream.flush();
        fOutputStream.close();
        return bmp;
    }

    private void showPreviewinButton(final Bitmap bmp) {
        runOnUiThread(new Runnable() {
            public void run() {
                PhotoPreview.setMaxWidth(bottonBar.getHeight() - 20);
                PhotoPreview.setMaxHeight(bottonBar.getHeight() - 20);
                PhotoPreview.setMinimumHeight(bottonBar.getHeight() - 20);
                PhotoPreview.setMinimumWidth(bottonBar.getHeight() - 20);
                PhotoPreview.setImageBitmap(bmp);
            }
        });
    }

    public void switchCamera(View ignored) {
        closeCamera();
        idCamera = (idCamera + 1) % 2;
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight(), idCamera);
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

    }

    private int adjustRotation(int rotation) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics;
        try {
            characteristics = manager.getCameraCharacteristics(mCameraId);
            if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT && rotation == 0) {
                rotation = (rotation + 2) % 4;
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();

        }
        return rotation;

    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height, idCamera);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = CameraView.this;
            activity.finish();
        }

    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            DisabelButton();
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage()));
        }

    };

    private final CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {

                    // We have nothing to do when the camera preview is working normally.
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState != null && !afState.equals(mLastAfState)) {
                        switch (afState) {
                            case CaptureResult.CONTROL_AF_STATE_INACTIVE:
                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_INACTIVE");
                                lockAutoFocus();
                                break;
                            case CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN:
                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN");
                                break;
                            case CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED:
                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED");
                                mUiHandler.removeCallbacks(mLockAutoFocusRunnable);
                                mUiHandler.postDelayed(mLockAutoFocusRunnable, LOCK_FOCUS_DELAY_ON_FOCUSED);
                                break;
                            case CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
                                mUiHandler.removeCallbacks(mLockAutoFocusRunnable);
                                mUiHandler.postDelayed(mLockAutoFocusRunnable, LOCK_FOCUS_DELAY_ON_UNFOCUSED);
                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED");
                                break;
                            case CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED:
                                mUiHandler.removeCallbacks(mLockAutoFocusRunnable);
                                //mUiHandler.postDelayed(mLockAutoFocusRunnable, LOCK_FOCUS_DELAY_ON_UNFOCUSED);
                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED");
                                break;
                            case CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN:
                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN");
                                break;
                            case CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED:
                                mUiHandler.removeCallbacks(mLockAutoFocusRunnable);
                                //mUiHandler.postDelayed(mLockAutoFocusRunnable, LOCK_FOCUS_DELAY_ON_FOCUSED);
                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED");
                                break;
                        }
                    }
                    mLastAfState = afState;
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            //CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState || CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            process(result);
        }

    };


    private static final long LOCK_FOCUS_DELAY_ON_FOCUSED = 5000;
    private static final long LOCK_FOCUS_DELAY_ON_UNFOCUSED = 1000;

    private Integer mLastAfState = null;
    private final Handler mUiHandler = new Handler(); // UI handler
    private final Runnable mLockAutoFocusRunnable = new Runnable() {

        @Override
        public void run() {
            lockAutoFocus();
        }
    };


    private void lockAutoFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            CaptureRequest captureRequest = mPreviewRequestBuilder.build();
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null); // prevent CONTROL_AF_TRIGGER_START from calling over and over again
            if (null != mCaptureSession) {
                mCaptureSession.capture(captureRequest, mCaptureCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void showToast(final String msg, final Context c) {
        Handler handler = new Handler(c.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }


    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w && option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight(), idCamera);
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();

    }

    private static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage) throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = null;
        try {
            imageStream = context.getContentResolver().openInputStream(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(img, selectedImage);
        return img;
    }

    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee a final image
            // with both dimensions larger than or equal to the requested height
            // and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }


    private String decodeUdc(String UPC) {
        String cur_Items = "";
        try {
            URL requestUrl = new URL("http://eandata.com/feed/?v=3&keycode=7F8C269A54623779&mode=json&find=0049000006582http://eandata.com/feed/?v=3&keycode=7F8C269A54623779&mode=json&find=" + UPC);
            URLConnection con = requestUrl.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            int cp;
            try {
                while ((cp = in.read()) != -1) {
                    sb.append((char) cp);
                }
            } catch (Exception ignore) {
            }
            String json = sb.toString();
            int posStart = json.indexOf("{\"attributes\":{\"product\":\"", 0);
            int posend = json.indexOf("\",\"description\"", 0);
            if (posStart > 0 && posend > 0)
                cur_Items = (String) json.subSequence(posStart + 26, posend);
            else {
                requestUrl = new URL("http://api.upcdatabase.org/xml/ff2e39bab6bdba2e3667db79b96ec318/" + UPC);
                con = requestUrl.openConnection();
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                sb = new StringBuilder();
                try {
                    while ((cp = in.read()) != -1) {
                        sb.append((char) cp);
                    }
                } catch (Exception ignore) {
                }
                json = sb.toString();
                if (json.contains("<description>"))
                    cur_Items = (String) json.subSequence(14, json.length() - 14);
            }
            if (cur_Items.equals("")) {
                requestUrl = new URL("http://www.upcdatabase.com/item/" + UPC);
                con = requestUrl.openConnection();
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                sb = new StringBuilder();
                try {
                    while ((cp = in.read()) != -1) {
                        sb.append((char) cp);
                    }
                } catch (Exception ignore) {
                }
                json = sb.toString();
                if (json.contains("Description"))
                    cur_Items = (String) json.subSequence(37, json.length() - 10);

            }
            if (cur_Items.equals(""))
                cur_Items = "Item not found";


        } catch (IOException e) {
            e.printStackTrace();
            cur_Items = "Item not found";
        }
        return cur_Items;
    }


    private class Connection extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {


            final String Udc = params[0];
            final String result = decodeUdc(Udc);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), Udc + " : " + result, Toast.LENGTH_LONG).show();

                }
            });

            return null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (!(result.getContents() == null)) {
                if (Collections.singletonList(IntentIntegrator.ONE_D_CODE_TYPES).get(0).contains(result.getFormatName())) {
                    myConnection = new Connection();
                    myConnection.execute(result.getContents());
                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), result.getContents(), Toast.LENGTH_LONG).show();

                        }
                    });
                }
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void setUpCameraOutputs(int width, int height, int id) {
        Activity activity = CameraView.this;
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = manager.getCameraIdList()[Math.min(manager.getCameraIdList().length - 1, id)];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // For still image captures, we use the largest available size.
            Size largest = Collections.max(Arrays.asList(map != null ? map.getOutputSizes(ImageFormat.JPEG) : new Size[0]), new CompareSizesByArea());
            mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 2);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);


            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, largest);


            mCameraId = cameraId;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not
            // supported on the
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(CameraView.this, "Permissions Granted", Toast.LENGTH_LONG)
                            .show();
                    finish();

                } else {
                    Toast.makeText(CameraView.this, "Permissions Denied", Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openCamera(int width, int height, int id) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        if (width <= height) {
            int tmp = height;
            height = width;
            width = tmp;
        }
        setUpCameraOutputs(width, height, id);
        configureTransform(width, height);
        Activity activity = CameraView.this;
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }


    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mUiHandler.removeCallbacks(mLockAutoFocusRunnable);
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(texture);
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) {
                        return;
                    }

                    mCaptureSession = cameraCaptureSession;
                    try {
                        if (isAutoFocusSupported())
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_AUTO);
                        else
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                        mPreviewRequest = mPreviewRequestBuilder.build();
                        if (null != mCaptureSession) {
                            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                        }
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    showToast("Failed", getApplicationContext());
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = CameraView.this;
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(), (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void takePicture() {
        lockFocus();
    }

    private void lockFocus() {
        try {
            if (mPreviewRequestBuilder != null) {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                mState = STATE_WAITING_LOCK;
                if (mCaptureSession != null)
                    mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void runPrecaptureSequence() {
        try {
            if (mPreviewRequestBuilder != null) {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                mState = STATE_WAITING_PRECAPTURE;
                if (mCaptureSession != null)
                    mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void captureStillPicture() {
        try {
            final Activity activity = CameraView.this;
            if (null == mCameraDevice) {
                return;
            }
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            rotation = adjustRotation(rotation);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    unlockFocus();
                }
            };
            if (null != mCaptureSession) {
                mCaptureSession.stopRepeating();
                mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        try {
            if (mPreviewRequestBuilder != null) {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                if (null != mCaptureSession) {
                    mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
                    mState = STATE_PREVIEW;
                    mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private class ImageSaver implements Runnable {

        private final Image mImage;

        public ImageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            currentLocation = null;
            if (remoteService != null)
                currentLocation = remoteService.getLocationTracker().getCurrentLocation();
            if (currentLocation == null) {
                Intent intentForAllert = new Intent(CameraView.this, LocationSettingActivity.class);
                intentForAllert.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentForAllert.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentForAllert);
                EnableButton();
            } else {
                long name = System.currentTimeMillis();
                addInDatabase(name);
                ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                FileOutputStream output = null;
                try {
                    File originalImage = new File(ImageFileLoc, Long.toString(name) + ".jpg");
                    output = new FileOutputStream(originalImage);
                    output.write(bytes);

                    Bitmap bmp = handleSamplingAndRotationBitmap(CameraView.this, Uri.fromFile(originalImage));
                    OutputStream fOut = new FileOutputStream(originalImage);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                    fOut.flush();
                    fOut.close(); // do not forget to close the stream

                    bmp = saveIcon(name, originalImage);


                    showPreviewinButton(bmp);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mImage.close();
                    EnableButton();
                    if (null != output) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

        }
    }

    private static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }

    }


    private boolean isAutoFocusSupported() {
        return isHardwareLevelSupported(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) || getMinimumFocusDistance() > 0;
    }

    private float getMinimumFocusDistance() {
        if (mCameraId == null)
            return 0;

        Float minimumLens = null;
        try {
            CameraManager manager = (CameraManager) this.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics c = manager.getCameraCharacteristics(mCameraId);
            minimumLens = c.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        } catch (Exception e) {
            Log.e(TAG, "isHardwareLevelSupported Error", e);
        }
        if (minimumLens != null)
            return minimumLens;
        return 0;
    }

    // Returns true if the device supports the required hardware level, or better.
    private boolean isHardwareLevelSupported(int requiredLevel) {
        boolean res = false;
        if (mCameraId == null)
            return false;
        try {
            CameraManager manager = (CameraManager) this.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(mCameraId);

            int deviceLevel = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            switch (deviceLevel) {
                //case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                //   Log.d(TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_3");
                //   break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                    Log.d(TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_FULL");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                    Log.d(TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                    Log.d(TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED");
                    break;
                default:
                    Log.d(TAG, "Unknown INFO_SUPPORTED_HARDWARE_LEVEL: " + deviceLevel);
                    break;
            }


            if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                res = requiredLevel == deviceLevel;
            } else {
                // deviceLevel is not LEGACY, can use numerical sort
                res = requiredLevel <= deviceLevel;
            }

        } catch (Exception e) {
            Log.e(TAG, "isHardwareLevelSupported Error", e);
        }
        return res;
    }

}

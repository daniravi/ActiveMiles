package org.imperial.activemilespro.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.imperial.activemilespro.database.PerformanceContentProvider;
import org.imperial.activemilespro.database.TableSingleActivity;
import org.imperial.activemilespro.gui.ActiveMilesGUI;
import org.imperial.activemilespro.interface_utility.IntActivityListener;
import org.imperial.activemilespro.interface_utility.UtilsCalendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.SystemClock;

public abstract class ActivityDetectorBase {

    private final ArrayList<Double> AccX;
    private final ArrayList<Double> AccY;
    private final ArrayList<Double> AccZ;
    private final ArrayList<Double> DifAccX;
    private final ArrayList<Double> DifAccY;
    private final ArrayList<Double> DifAccZ;
    private final ArrayList<Double> DifGyroX;
    private final ArrayList<Double> DifGyroY;
    private final ArrayList<Double> DifGyroZ;
    private final ArrayList<Double> GyroX;
    private final ArrayList<Double> GyroY;
    private final ArrayList<Double> GyroZ;
    private double lastAccX = 0;
    private double lastAccY = 0;
    private double lastAccZ = 0;
    private double lastGyroX = 0;
    private double lastGyroY = 0;
    private double lastGyroZ = 0;
    private long LastTimeStemp = 0;
    private BufferedWriter writer;

    public static final int sizeOfSegment = 5000;
    private static final int saveAtLeatEach = 1000 * 60 * 3;
    private static final int minThreshold = 10;
    private static final int ConfidenceThreshold = 2;

    private final ArrayList<IntActivityListener> mActivityListeners = new ArrayList<>();
    private boolean isClassifing = false;
    private int previewActivity;
    private int currentActivity;
    private static long StartActivity;

    private final StepDetector myStepDetector;
    private int curr_numberOfStepInSegment = 0;
    private int curr_numberOfStep = 0;
    final Context c;
    private final SensorDataServiceBase sensorBase;
    private final double[] confidence = new double[ActiveMilesGUI.NumberOfActivities];
    private int numOfSegmentForCurrentActivity = 0;


    public native String FFT();

    long torchState = 0;

    public native String callTorch(long stateLocation, double[] data, int size);

    public abstract void init(AssetManager Am, String s);

    public native void destroyTorch(long stateLocation);

    static {
        System.loadLibrary("torchdemo");
    }

    @Override
    public void finalize() {
        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        closeTorch();
    }

    public void setWriter(BufferedWriter w) {
        writer=w;
    }


    public void closeTorch() {
        if (torchState != 0)
            destroyTorch(torchState);
        torchState = 0;
    }

    public void onGyroChanged(float g1, float g2, float g3) {

        synchronized (this) {

            if (!isClassifing) {

                DifGyroX.add((double) g1 - lastGyroX);
                lastGyroX = g1;
                DifGyroY.add((double) g2 - lastGyroY);
                lastGyroY = g2;
                DifGyroZ.add((double) g3 - lastGyroZ);
                lastGyroZ = g3;

                GyroX.add((double) g1);
                GyroY.add((double) g2);
                GyroZ.add((double) g3);

            }

        }
    }

    public void onAccChanged(float a1, float a2, float a3) {

        synchronized (this) {
            float[] myArray = {a1, a2, a3};
            if (!isClassifing) {

                DifAccX.add((double) a1 - lastAccX);
                lastAccX = a1;
                DifAccY.add((double) a2 - lastAccY);
                lastAccY = a2;
                DifAccZ.add((double) a3 - lastAccZ);
                lastAccZ = a3;
                if ((ActiveMilesGUI.Activities[currentActivity].compareTo("Walking") == 0) || (ActiveMilesGUI.Activities[currentActivity].compareTo("Running") == 0))
                    curr_numberOfStepInSegment += myStepDetector.isstep(myArray);
                AccX.add((double) a1);
                AccY.add((double) a2);
                AccZ.add((double) a3);
                if (SystemClock.elapsedRealtime()  - LastTimeStemp > sizeOfSegment) {

                    LastTimeStemp = SystemClock.elapsedRealtime() ;
                        ActivityClasify();
                }
            }

        }
    }

    ActivityDetectorBase(Context context, SensorDataServiceBase sensorBase, int sensitivity) {
        AccX = new ArrayList<>();
        AccY = new ArrayList<>();
        AccZ = new ArrayList<>();
        GyroX = new ArrayList<>();
        GyroY = new ArrayList<>();
        GyroZ = new ArrayList<>();
        DifAccX = new ArrayList<>();
        DifAccY = new ArrayList<>();
        DifAccZ = new ArrayList<>();
        DifGyroX = new ArrayList<>();
        DifGyroY = new ArrayList<>();
        DifGyroZ = new ArrayList<>();

        LastTimeStemp = SystemClock.elapsedRealtime() ;
        StartActivity=LastTimeStemp;
        myStepDetector = new StepDetector(sensitivity);
        this.sensorBase = sensorBase;
        c = context;
    }

    public void changeSensitivity(int sensitivity) {
        myStepDetector.changeSensitivity(sensitivity);
    }

    public void addActivityListener(IntActivityListener sl) {
        mActivityListeners.add(sl);
    }


    private void ActivityClasify() {

        synchronized (this) {

            if (!isClassifing) {
                isClassifing = true;
                double currMax = -100;
                int indexMax = -1;
                if (GyroX.size() > minThreshold && AccX.size() > minThreshold) {

                    DifAccX.remove(0);
                    DifAccY.remove(0);
                    DifAccZ.remove(0);
                    DifGyroX.remove(0);
                    DifGyroY.remove(0);
                    DifGyroZ.remove(0);


                    /*if (this.sensorBase.DeviceType == 1) {
                        double[] arrayOfFeat = {Max(GyroX), Mean(GyroY), Skewness(GyroY), Skewness(GyroZ), Kurtosis(GyroZ), zeroCrossing(GyroX), Iqr(GyroZ), Median(GyroX), Rms(GyroY), Std(DifAccY),
                                Max(AccX), Min(AccX), Min(AccY), Min(AccZ), Max(AccY) - Min(AccY), Mean(AccZ), Kurtosis(AccY), Kurtosis(AccZ), MeanCross(AccY), Median(AccZ), Rms(AccX), Rms(AccY),
                                Rms(AccZ)};
                        if (torchState == 0)
                            init(c.getAssets(), c.getApplicationInfo().nativeLibraryDir);
                        time = callTorch(torchState, arrayOfFeat, Array.getLength(arrayOfFeat));
                    } else if (this.sensorBase.DeviceType == 2) {*/
                    double[] arrayOfFeat = {Mean(DifGyroX), Mean(DifGyroY), Mean(DifGyroZ), Variance(DifGyroX), Variance(DifGyroY), Variance(DifGyroZ), Std(DifGyroX), Std(DifGyroY),
                            Std(DifGyroZ), Rms(DifGyroX), Rms(DifGyroY), Std(DifGyroZ), Max(GyroX), Max(GyroY), Max(GyroZ), Min(GyroX), Min(GyroY), Min(GyroZ), Max(GyroX) - Min(GyroX),
                            Max(GyroY) - Min(GyroY), Max(GyroZ) - Min(GyroZ), Std(GyroX), Std(GyroY), Std(GyroZ), Mean(GyroX), Mean(GyroY), Mean(GyroZ), Variance(GyroX), Variance(GyroY),
                            Variance(GyroZ), Skewness(GyroX), Skewness(GyroY), Skewness(GyroZ), Kurtosis(GyroX), Kurtosis(GyroY), Kurtosis(GyroZ), zeroCrossing(GyroX), zeroCrossing(GyroY),
                            zeroCrossing(GyroZ), MeanCross(GyroX), MeanCross(GyroY), MeanCross(GyroZ), Iqr(GyroX), Iqr(GyroY), Iqr(GyroZ), Median(GyroX), Median(GyroY), Median(GyroZ), Rms(GyroX),
                            Rms(GyroY), Rms(GyroZ),

                            Mean(DifAccX), Mean(DifAccY), Mean(DifAccZ), Variance(DifAccX), Variance(DifAccY), Variance(DifAccZ), Std(DifAccX), Std(DifAccY), Std(DifAccZ), Rms(DifAccX),
                            Rms(DifAccY), Std(DifAccZ), Max(AccX), Max(AccY), Max(AccZ), Min(AccX), Min(AccY), Min(AccZ), Max(AccX) - Min(AccX), Max(AccY) - Min(AccY), Max(AccZ) - Min(AccZ),
                            Std(AccX), Std(AccY), Std(AccZ), Mean(AccX), Mean(AccY), Mean(AccZ), Variance(AccX), Variance(AccY), Variance(AccZ), Skewness(AccX), Skewness(AccY), Skewness(AccZ),
                            Kurtosis(AccX), Kurtosis(AccY), Kurtosis(AccZ), zeroCrossing(AccX), zeroCrossing(AccY), zeroCrossing(AccZ), MeanCross(AccX), MeanCross(AccY), MeanCross(AccZ),
                            Iqr(AccX), Iqr(AccY), Iqr(AccZ), Median(AccX), Median(AccY), Median(AccZ), Rms(AccX), Rms(AccY), Rms(AccZ),};
                    if (torchState == 0)
                        init(c.getAssets(), c.getApplicationInfo().nativeLibraryDir);

                    String time = callTorch(torchState, arrayOfFeat, Array.getLength(arrayOfFeat));
                    //}

                    try {
                        time = time.replace("[", "");
                        time = time.replace("  ", " ");
                        time = time.replace("  ", " ");

                        StringBuilder TimeBuffer = new StringBuilder(time);
                        while (TimeBuffer.indexOf("Columns") != -1) {
                            int strToremoveIndex = TimeBuffer.indexOf("Columns");
                            TimeBuffer.replace(strToremoveIndex, TimeBuffer.indexOf("\n", strToremoveIndex) + 1, "");
                        }
                        time = TimeBuffer.toString();
                        String[] predic_confid = time.split(",");
                        predic_confid[0] = predic_confid[0].substring(1, predic_confid[0].indexOf("torch") - 1);
                        predic_confid[1] = predic_confid[1].substring(1, predic_confid[1].indexOf("torch") - 1);

                        String[] predic = predic_confid[1].split(" ");
                        String[] confid = predic_confid[0].split(" ");

                        for (int i = 0; i < predic.length; i++)
                            confidence[Integer.parseInt(predic[i])] += Math.exp(Double.parseDouble(confid[i]) / (i + 1));
                    } catch (NumberFormatException ignored) {
                    }
                    for (int i = 0; i < ActiveMilesGUI.NumberOfActivities; i++)
                        if (confidence[i] > currMax && ActiveMilesGUI.ActivitiesSelected[i]) {
                            currMax = confidence[i];
                            indexMax = i;

                        }
                } else {
                    currMax = ConfidenceThreshold + 1;
                    indexMax = 6;
                }
                if (currMax > ConfidenceThreshold) {
                    currentActivity = indexMax;
                    if (sensorBase.LiveViewisOn) {
                        int[] graphDataForView = new int[ActiveMilesGUI.NumberOfActivities];
                        graphDataForView[currentActivity] = currentActivity;
                        sensorBase.lv.addActivity(graphDataForView, currentActivity, numOfSegmentForCurrentActivity);
                        try {
                            if (writer!=null) {
                                writer.write("HAR>" +   ActiveMilesGUI.ActivitiesToShow[currentActivity]+ ',' + numOfSegmentForCurrentActivity);
                                writer.newLine();
                                writer.flush();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                    String currentTimeStep = UtilsCalendar.currTimeToStringSec();
                    Uri uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.GET_SINGLE_ACTIVITY);
                    ContentValues values = new ContentValues();
                    values.put(TableSingleActivity.COLUMN_ACTIVITY, currentActivity);
                    values.put(TableSingleActivity.COLUMN_NUMB_SEGMENTS, numOfSegmentForCurrentActivity);
                    values.put(TableSingleActivity.COLUMN_TIME_STEMP, currentTimeStep);
                    c.getContentResolver().insert(uri, values);

                    for (int j = 0; j < ActiveMilesGUI.NumberOfActivities; j++)
                        confidence[j] = 0;

                    curr_numberOfStepInSegment = 0;
                    if (ActiveMilesGUI.Activities[currentActivity].compareTo("Walking") == 0)
                        curr_numberOfStepInSegment = Math.min(Math.max(curr_numberOfStepInSegment, 3 * numOfSegmentForCurrentActivity), 10 * 5000 / sizeOfSegment);/* Just Limit the number of step*/
                    if (ActiveMilesGUI.Activities[currentActivity].compareTo("Running") == 0)
                        curr_numberOfStepInSegment = Math.min(Math.max(curr_numberOfStepInSegment, 8 * numOfSegmentForCurrentActivity), 20 * 5000 / sizeOfSegment);/* Just Limit the number of step*/
                    curr_numberOfStep += curr_numberOfStepInSegment;

                    long currentTime = SystemClock.elapsedRealtime();
                    if ((previewActivity != currentActivity) || Math.abs(StartActivity - currentTime) > saveAtLeatEach) {
                        mActivityListeners.get(0).onActivityDetected(previewActivity, (int) Math.abs(StartActivity - currentTime) / sizeOfSegment, curr_numberOfStep);
                        previewActivity = currentActivity;
                        StartActivity = currentTime;
                        curr_numberOfStep = 0;
                    }
                    numOfSegmentForCurrentActivity = 1;

                }
                numOfSegmentForCurrentActivity++;

                GyroX.clear();
                GyroY.clear();
                GyroZ.clear();
                AccX.clear();
                AccY.clear();
                AccZ.clear();
                DifAccX.clear();
                DifAccY.clear();
                DifAccZ.clear();
                DifGyroX.clear();
                DifGyroY.clear();
                DifGyroZ.clear();

            }
            isClassifing = false;
        }
    }

    private double zeroCrossing(ArrayList<Double> array) {
        int i;
        boolean sign1, sign2;
        double result = 0;

        for (i = 0; i < array.size() - 1; i++) {
            sign1 = getSign(array.get(i));
            sign2 = getSign(array.get(i + 1));
            if (sign1 != sign2)
                result++;
        }
        return result;
    }


    private double MeanCross(ArrayList<Double> array) {
        int i;
        boolean sign1, sign2;
        double result = 0;
        double mean = Mean(array);
        for (i = 0; i < array.size() - 1; i++) {
            sign1 = array.get(i) > mean;
            sign2 = array.get(i + 1) > mean;
            if (sign1 != sign2)
                result++;
        }
        return result;
    }

    private boolean getSign(Double data) {
        return (data > 0);
    }

    private double Min(ArrayList<Double> array) {
        return Collections.min(array);
    }

    private double Max(ArrayList<Double> array) {
        return Collections.max(array);
    }

    private double Mean(ArrayList<Double> array) {
        double sum = 0.0;
        for (double a : array)
            sum += a;
        return sum / array.size();
    }

    private double Variance(ArrayList<Double> array) {
        double mean = Mean(array);
        double temp = 0;
        for (double a : array)
            temp += (mean - a) * (mean - a);
        return temp / array.size();
    }

    private double Std(ArrayList<Double> array) {
        return Math.sqrt(Variance(array));
    }

    private double Median(ArrayList<Double> array) {
        Collections.sort(array);
        int middle = array.size() / 2;
        if (array.size() % 2 == 1) {
            return array.get(middle);
        } else {
            return (array.get(middle - 1) + array.get(middle)) / 2.0;
        }
    }

    private double Rms(ArrayList<Double> array) {
        double t = 0;
        double x;
        int n = array.size();
        for (int i = 0; i < n; i++) {
            x = array.get(i);
            t = t + Math.pow(x, 2);
        }
        if ((t != 0))
            return Math.sqrt(t / n);
        else
            return 0;
    }

    private double Kurtosis(ArrayList<Double> array) {
        Kurtosis k = new Kurtosis();
        double[] normalArray = new double[array.size()];
        for (int i = 0; i < array.size(); i++)
            normalArray[i] = array.get(i);
        double result = k.evaluate(normalArray);
        if (String.valueOf(result).equals("NaN"))
            return 0;
        else
            return result;
    }

    private double Skewness(ArrayList<Double> array) {
        Skewness k = new Skewness();
        double[] normalArray = new double[array.size()];
        for (int i = 0; i < array.size(); i++)
            normalArray[i] = array.get(i);
        double result = k.evaluate(normalArray);
        if (String.valueOf(result).equals("NaN"))
            return 0;
        else
            return result;
    }

    private double Iqr(ArrayList<Double> array) {
        double[] quartiles = Quartiles(array);
        return quartiles[2] - quartiles[0];
    }

    private double[] Quartiles(ArrayList<Double> values) {
        if (values.size() < 3)
            return null;

        double median = Median(values);

        ArrayList<Double> lowerHalf = GetValuesLessThan(values, median, true);
        ArrayList<Double> upperHalf = GetValuesGreaterThan(values, median, true);

        return new double[]{Median(lowerHalf), median, Median(upperHalf)};
    }

    private ArrayList<Double> GetValuesGreaterThan(ArrayList<Double> values, double limit, boolean orEqualTo) {
        ArrayList<Double> modValues = new ArrayList<>();

        for (double value : values)
            if (value > limit || (value == limit && orEqualTo))
                modValues.add(value);

        return modValues;
    }

    private ArrayList<Double> GetValuesLessThan(ArrayList<Double> values, double limit, boolean orEqualTo) {
        ArrayList<Double> modValues = new ArrayList<>();

        for (double value : values)
            if (value < limit || (value == limit && orEqualTo))
                modValues.add(value);

        return modValues;
    }

}
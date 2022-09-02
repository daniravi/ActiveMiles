package org.imperial.activemilespro.service;


import android.hardware.SensorManager;
import android.util.Log;

public class StepDetector {

    public static final double[] sensitivityList = {1.97, 2.96, 4.44, 6.66, 10.00, 15.00, 22.50, 33.75, 50.62};
    private final float[] mLastValues = new float[3 * 2];
    private final float[] mScale = new float[2];
    private static final int h = 480;
    private static final float mYOffset = h * 0.5f;
    private final float[] mLastDirections = new float[3 * 2];
    private final float[][] mLastExtremes = {new float[3 * 2], new float[3 * 2]};
    private final float[] mLastDiff = new float[3 * 2];
    private int mLastMatch = -1;
    private int sensitivity;
    private static final String TAG = "StepDetector";

    public StepDetector(int sensitivity) {
        this.sensitivity = sensitivity;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
    }

    public void changeSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
        Log.d(TAG, sensitivity + "");
    }

    public int isstep(float[] values) {
        int isStep = 0;
        float vSum = 0;
        for (int i = 0; i < 3; i++) {
            final float v = mYOffset + values[i] * mScale[1];
            vSum += v;
        }
        int k = 0;
        float v = vSum / 3;

        float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
        if (direction == -mLastDirections[k]) {
            // Direction changed
            int extType = (direction > 0 ? 0 : 1); // minumum or
            // maximum?
            mLastExtremes[extType][k] = mLastValues[k];
            float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

            if (diff > sensitivityList[sensitivity]) {

                boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
                boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
                boolean isNotContra = (mLastMatch != 1 - extType);

                if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                    isStep = 1;
                    mLastMatch = extType;
                } else {
                    mLastMatch = -1;
                }
            }
            mLastDiff[k] = diff;
        }
        mLastDirections[k] = direction;
        mLastValues[k] = v;
        return isStep;
    }

	/* CODE FROM OLD CHINESE
    protected double getDistance(ArrayList<Double> x, ArrayList<Double> y, ArrayList<Double> z, double k)
	{

		double[] b = new double[] { 0.020083365564211, 0.040166731128423, 0.020083365564211 };
		double[] a = new double[] { 1.000000000000000, -1.561018075800718, 0.641351538057563 };
		double parmThreshold = 2;

		double[] normyy = new double[x.size()];
		ArrayList<Double[]> maxtab = new ArrayList<>();
		ArrayList<Double[]> mintab = new ArrayList<>();

		for (int i = 0; i < x.size(); i++)
			normyy[i] = Math.sqrt(Math.pow(x.get(i), 2) + Math.pow(y.get(i), 2) + Math.pow(z.get(i), 2));

		double[] normyy2 = filter(normyy, b, a);

		peakdet(normyy2, parmThreshold, maxtab, mintab);
		ArrayList<Double> amplittude = maxMin(maxtab, mintab);
		double sumAmplitude = 0;
		for (int i = 0; i < mintab.size(); i++)
		{
			sumAmplitude += Math.pow(amplittude.get(i), 0.25);
		}
		return sumAmplitude * k;

	}

	protected int getNumberOfStep(ArrayList<Double> x, ArrayList<Double> y, ArrayList<Double> z)
	{
		double[] b = new double[] { 0.020083365564211, 0.040166731128423, 0.020083365564211 };
		double[] a = new double[] { 1.000000000000000, -1.561018075800718, 0.641351538057563 };
		double parmThreshold = 1.5;

		double[] normyy = new double[x.size()];
		ArrayList<Double[]> maxtab = new ArrayList<>();
		ArrayList<Double[]> mintab = new ArrayList<>();

		for (int i = 0; i < x.size(); i++)
			normyy[i] = Math.sqrt(Math.pow(x.get(i), 2) + Math.pow(y.get(i), 2) + Math.pow(z.get(i), 2));
		double[] normyy2 = filter(normyy, b, a);
		peakdet(normyy2, parmThreshold, maxtab, mintab);
		return mintab.size();
	}

    private void peakdet(double[] v, double delta, ArrayList<Double[]> maxtab, ArrayList<Double[]> mintab) {
        double[] speed = new double[v.length];
        int[] x = new int[v.length];
        for (int i = 0; i < v.length; i++) {
            speed[i] = 5;
            x[i] = i;
        }
        int lookformax = 1;
        double mn = Double.MAX_VALUE;
        double mx = -Double.MAX_VALUE;
        int mnpos = -1;
        int mxpos = -1;
        double currValue;
        double lastmx;
        double lastmn;
        int temp;
        for (int i = 0; i < v.length; i++) {
            currValue = v[i];

            if (currValue > mx) {
                mx = currValue;
                mxpos = x[i];
            }
            if (currValue < mn) {
                mn = currValue;
                mnpos = x[i];
            }
            if (lookformax == 1) {
                if (currValue < mx - delta) {
                    lookformax = 0;
                    mn = currValue;
                    mnpos = x[i];
                    temp = maxtab.size();
                    if (temp > 0 && speed[mxpos] > 1) {
                        lastmx = maxtab.get(temp - 1)[0];
                        if (mxpos - lastmx >= 4) {
                            Double[] CurrMaxTab = {(double) mxpos, mx};
                            maxtab.add(CurrMaxTab);
                        }

                    }
                    if (temp == 0 && speed[mxpos] > 1) {
                        Double[] CurrMaxTab = {(double) mxpos, mx};
                        maxtab.add(CurrMaxTab);
                    }

                }
            } else {
                if (currValue > mn + delta) {
                    lookformax = 1;
                    mx = currValue;
                    mxpos = x[i];
                    temp = mintab.size();
                    if (temp > 0 && speed[mnpos] > 1) {
                        lastmn = mintab.get(temp - 1)[0];
                        if (mnpos - lastmn >= 4) {
                            Double[] CurrMinTab = {(double) mnpos, mn};
                            mintab.add(CurrMinTab);

                        }
                    }
                    if (temp == 0 && speed[mnpos] > 1) {
                        Double[] CurrMinTab = {(double) mnpos, mn};
                        mintab.add(CurrMinTab);
                    }
                }
            }

        }
    }

    private double[] filter(double[] inputValue, double[] b, double[] a) {

        int pos1 = 0;
        int pos2 = 0;
        int n1 = b.length - 1;
        int n2 = a.length - 1;
        double[] buf1 = new double[n1];
        double[] buf2 = new double[n2];
        double[] y = new double[inputValue.length];

        for (int i = 0; i < inputValue.length; i++) {
            double acc = b[0] * inputValue[i];
            for (int j = 1; j <= n1; j++) {
                int p = (pos1 + n1 - j) % n1;
                acc += b[j] * buf1[p];
            }
            for (int j = 1; j <= n2; j++) {
                int p = (pos2 + n2 - j) % n2;
                acc -= a[j] * buf2[p];
            }
            if (n1 > 0) {
                buf1[pos1] = inputValue[i];
                pos1 = (pos1 + 1) % n1;
            }
            if (n2 > 0) {
                buf2[pos2] = acc;
                pos2 = (pos2 + 1) % n2;
            }
            y[i] = acc;
        }
        return y;
    }

    private ArrayList<Double> maxMin(ArrayList<Double[]> maxtab, ArrayList<Double[]> mintab) {

        ArrayList<Double> ampitude = new ArrayList<>();
        for (int i = 0; i < maxtab.size(); i++)
            ampitude.add(Math.abs(maxtab.get(i)[1] - mintab.get(i)[1]));
        return ampitude;
    }
    */

}

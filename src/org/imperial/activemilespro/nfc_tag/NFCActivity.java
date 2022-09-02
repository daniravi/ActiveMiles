package org.imperial.activemilespro.nfc_tag;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.imperial.activemilespro.R;


@SuppressLint("NewApi")
public class NFCActivity extends Activity {

    public static Ntag_I2C_Demo demo;

    private boolean newIntent = false;

    private TextView mDataOne;
    private TextView mDataTwo;
    private TextView mDataThree;
    private TextView mDataType;
    private TextView mDataId;
    private TextView mDataLatitude;
    private TextView mDataLongitude;
    private LineGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;
    private GraphView graph1;
    private double graph1LastValue1 = 5d;
    private double graph1LastValue2 = 5d;

    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private NfcAdapter mAdapter;

    File file;

    private LocationManager locationManager;
    private String provider;
    float latitude = -91f;
    float longitude = -181f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);


        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsStatus) {
            Settings.Secure.putString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, "gps");
        }


        // Define the criteria how to select the location provider -> use default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, true);
        //provider = locationManager.GPS_PROVIDER;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            onLocationChanged(location);

        } else {
            latitude = -91f;
            longitude = -181f;
        }


        // GUI
        mDataType = (TextView) findViewById(R.id.display_type);
        mDataId = (TextView) findViewById(R.id.display_id);
        mDataOne = (TextView) findViewById(R.id.display_data_one);
        mDataTwo = (TextView) findViewById(R.id.display_data_two);
        mDataThree = (TextView) findViewById(R.id.display_data_three);
        mDataLatitude = (TextView) findViewById(R.id.display_latitude);
        mDataLongitude = (TextView) findViewById(R.id.display_longitude);

        LinearLayout layout1 = (LinearLayout) this.findViewById(R.id.graph1) ;
        graph1 = new GraphView(this);
        mSeries1 = new LineGraphSeries<DataPoint>();
        mSeries1.setColor(Color.RED);
        mSeries2 = new LineGraphSeries<DataPoint>();
        mSeries2.setColor(Color.BLUE);
        graph1.addSeries(mSeries1);
        graph1.addSeries(mSeries2);
        graph1.getViewport().setYAxisBoundsManual(false);
        graph1.getViewport().setXAxisBoundsManual(true);
        graph1.getViewport().setMinX(0);
        graph1.getViewport().setMaxX(20);
        graph1.getViewport().setMinY(0);
        graph1.getViewport().setMaxY(300);
        graph1.getGridLabelRenderer().setVerticalLabelsVisible(true);
        graph1.getGridLabelRenderer().setPadding(50);
        graph1.getGridLabelRenderer().setTextSize(22);
        graph1.setTitle("Spectroscopy");
        layout1.addView(graph1);


        // initialize the demo in order to handle events
        demo = new Ntag_I2C_Demo(null, this);

        // Check for available NFC Adapters
        mAdapter = NfcAdapter.getDefaultAdapter(this);

        setNfcForeground();




    }

    public void setNfcForeground() {
        // create a generic PendindIntent that will be delivered to this activity. The NFC stack will fill
        // in the intent with the details of the discovered tag before delivering it to this activity
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(), getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // setup an intent filter for all NDEF based dispatches

        mFilters = new IntentFilter[]{
                //new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)};

        // Setup a tech list for all NFC tags
        mTechLists = new String[][]{new String[]{NfcA.class.getName()}};


    }


    private ReaderCallback createReaderCallback(final Activity main) {
        return new ReaderCallback() {
            @Override
            public void onTagDiscovered(final Tag tag) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        demo = new Ntag_I2C_Demo(tag, main);
                        if (demo.isReady()) {

                            try {
                                Thread.sleep(300);
                                //Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            Calendar calendar = Calendar.getInstance();
                            DateFormat df = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
                            String date = df.format(Calendar.getInstance().getTime());

                            erase_file_content();


                            String str2 = "reading";

                            writeToFile2(str2);

                            writeToFile2(date);
                            writeToFile2(Float.toString(latitude));
                            writeToFile2(Float.toString(longitude));

                            int a_index;
                            int type_sensor;
                            int id_sensor;
                            int voltage_channel_one;
                            int voltage_channel_two;
                            int temperature;
                            int temp_data;

                            byte[] msg = demo.readTagContent();

                            temp_data = (int) (msg[19] & 0xFF);
                            type_sensor = temp_data;

                            temp_data = (int) (msg[20] & 0xFF);
                            id_sensor = temp_data;

                            temp_data = (int) (msg[21] & 0xFF);
                            temp_data = temp_data<<8;
                            voltage_channel_one = temp_data;
                            temp_data = (int) (msg[22] & 0xFF);
                            voltage_channel_one = voltage_channel_one + temp_data;

                            temp_data = (int) (msg[23] & 0xFF);
                            temp_data = temp_data<<8;
                            voltage_channel_two = temp_data;
                            temp_data = (int) (msg[24] & 0xFF);
                            voltage_channel_two = voltage_channel_two + temp_data;

                            temp_data = (int) (msg[25] & 0xFF);
                            temp_data = temp_data<<8;
                            temperature = temp_data;
                            temp_data = (int) (msg[26] & 0xFF);
                            temperature = temperature + temp_data;

                            writeToFile(Integer.toString(type_sensor));
                            writeToFile(Integer.toString(id_sensor));
                            writeToFile(Integer.toString(voltage_channel_one));
                            writeToFile(Integer.toString(voltage_channel_two));
                            writeToFile(Integer.toString(temperature));

                            for (a_index=32;a_index<47;a_index++)
                            {
                                temp_data = (int) (msg[a_index] & 0xFF);
                                mSeries1.appendData(new DataPoint(graph1LastValue1,temp_data),true,20);
                                writeToFile(Integer.toString(temp_data));
                                graph1LastValue1 += 1d;
                            }

                            for (a_index=48;a_index<63;a_index++)
                            {
                                temp_data = (int) (msg[a_index] & 0xFF);
                                mSeries2.appendData(new DataPoint(graph1LastValue2,temp_data),true,20);
                                writeToFile(Integer.toString(temp_data));
                                graph1LastValue2 += 1d;
                            }

                            writeToFile2("end");

                            mDataId.setText(Integer.toString(id_sensor));
                            mDataType.setText(Integer.toString(type_sensor));
                            mDataLatitude.setText(Float.toString(latitude));
                            mDataLongitude.setText(Float.toString(longitude));
                            mDataOne.setText(Integer.toString(voltage_channel_one));
                            mDataTwo.setText(Integer.toString(voltage_channel_two));
                            mDataThree.setText(Integer.toString(temperature));



                        }
                    }
                });

            }
        };
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();

        if (mAdapter != null) {
            Bundle options = new Bundle();

            if (Build.VERSION.SDK_INT >= 19) {
                // This option can increase the presence check delay
                mAdapter.enableReaderMode(this, createReaderCallback(this), NfcAdapter.FLAG_READER_NFC_A, Bundle.EMPTY);
            } else {
                mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
            }
        }

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()) && newIntent == false) {
            // Give the UI some time to load, then execute the application
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onNewIntent(getIntent());
                }
            }, 100);
        }
        newIntent = false;


    }

    @SuppressLint("NewApi")
    @Override
    public void onPause() {
        super.onPause();

        if (mAdapter != null && newIntent == false) {
            if (Build.VERSION.SDK_INT >= 19)
                mAdapter.disableReaderMode(this);
            else
                mAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent (Intent nfc_intent)  {
        newIntent = true;
        super.onNewIntent(nfc_intent);
        // Set the parameters for vibration
        long pattern[] = {0, 100};

        // vibrate on new Intent
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern,-1);

        doProcess(nfc_intent);

    }

    public void doProcess (Intent nfc_intent) {
        final Tag tag = nfc_intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        demo = new Ntag_I2C_Demo(tag,this);
        if (demo.isReady()) {

            Calendar calendar = Calendar.getInstance();
            DateFormat df = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
            String date = df.format(Calendar.getInstance().getTime());

            erase_file_content();

            String str2 = "reading";

            writeToFile2(str2);

            writeToFile2(date);
            writeToFile2(Float.toString(latitude));
            writeToFile2(Float.toString(longitude));

            int a_index;
            int type_sensor;
            int id_sensor;
            int voltage_channel_one;
            int voltage_channel_two;
            int temperature;
            int temp_data;

            byte[] msg = demo.readTagContent();

            temp_data = (int) (msg[19] & 0xFF);
            type_sensor = temp_data;

            temp_data = (int) (msg[20] & 0xFF);
            id_sensor = temp_data;

            temp_data = (int) (msg[21] & 0xFF);
            temp_data = temp_data<<8;
            voltage_channel_one = temp_data;
            temp_data = (int) (msg[22] & 0xFF);
            voltage_channel_one = voltage_channel_one + temp_data;

            temp_data = (int) (msg[23] & 0xFF);
            temp_data = temp_data<<8;
            voltage_channel_two = temp_data;
            temp_data = (int) (msg[24] & 0xFF);
            voltage_channel_two = voltage_channel_two + temp_data;

            temp_data = (int) (msg[25] & 0xFF);
            temp_data = temp_data<<8;
            temperature = temp_data;
            temp_data = (int) (msg[26] & 0xFF);
            temperature = temperature + temp_data;

            writeToFile(Integer.toString(type_sensor));
            writeToFile(Integer.toString(id_sensor));
            writeToFile(Integer.toString(voltage_channel_one));
            writeToFile(Integer.toString(voltage_channel_two));
            writeToFile(Integer.toString(temperature));

            for (a_index=32;a_index<47;a_index++)
            {
                temp_data = (int) (msg[a_index] & 0xFF);
                mSeries1.appendData(new DataPoint(graph1LastValue1,temp_data),true,20);
                writeToFile(Integer.toString(temp_data));
                graph1LastValue1 += 1d;
            }

            for (a_index=48;a_index<63;a_index++)
            {
                temp_data = (int) (msg[a_index] & 0xFF);
                mSeries2.appendData(new DataPoint(graph1LastValue2,temp_data),true,20);
                writeToFile(Integer.toString(temp_data));
                graph1LastValue2 += 1d;
            }

            writeToFile2("end");

            mDataId.setText(Integer.toString(id_sensor));
            mDataType.setText(Integer.toString(type_sensor));
            mDataLatitude.setText(Float.toString(latitude));
            mDataLongitude.setText(Float.toString(longitude));
            mDataOne.setText(Integer.toString(voltage_channel_one));
            mDataTwo.setText(Integer.toString(voltage_channel_two));
            mDataThree.setText(Integer.toString(temperature));


        }
    }

    private void writeToFile (String data_in)
    {
        FileOutputStream outputStream;

        try {
            file = new File(Environment.getExternalStorageDirectory(),"NFC_recordings.txt");
            outputStream = new FileOutputStream(file,true); // append
            outputStream.write(data_in.getBytes());
            outputStream.write(" ".getBytes());
            outputStream.close();
        }catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void writeToFile2 (String data_in)
    {
        FileOutputStream outputStream;

        try {
            file = new File(Environment.getExternalStorageDirectory(),"NFC_recordings.txt");
            outputStream = new FileOutputStream(file,true); // append
            outputStream.write(data_in.getBytes());
            outputStream.write("\n".getBytes());
            outputStream.close();
        }catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void erase_file_content ()
    {
        FileOutputStream outputStream;

        try {
            file = new File(Environment.getExternalStorageDirectory(),"NFC_recordings.txt");
            outputStream = new FileOutputStream(file,false); // remove previous files
            outputStream.close();
        }catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    public void onLocationChanged(Location location) {
        latitude = (float) (location.getLatitude());
        longitude = (float) (location.getLongitude());

    }


}

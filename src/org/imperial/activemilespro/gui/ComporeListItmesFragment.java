package org.imperial.activemilespro.gui;

import java.util.ArrayList;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.interface_utility.IntDrowableContainer;
import org.imperial.activemilespro.interface_utility.MyInteger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ComporeListItmesFragment extends Fragment implements IntDrowableContainer {

    private ArrayList<String> listUidFbUsers;
    private ArrayList<String> listNameFbUsers;
    public ArrayList<int[]> listHistPerformance;
    public ArrayList<MyInteger> listFinalPerformance;
    private ArrayList<Drawable> listHistView;
    private Context mContex;
    private GridAdapter myAdapter;
    private int color;
    private ProgressDialog progress;
    private int numbOfComparisonDownloaded;
    private Thread progresBarTrhead;

    public void update() {
        if (myAdapter != null)
            myAdapter.notifyDataSetChanged();
        numbOfComparisonDownloaded++;
        if (numbOfComparisonDownloaded == listUidFbUsers.size() * 2)
            closeProgressBar();

    }


    private void openProgressBar() {
        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                progress.setIndeterminate(false);
                progress.setCancelable(false);
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setMessage("Loading...");
                progress.show();
            }
        });

        final int totalProgressTime = 100;
        progresBarTrhead = new Thread() {

            @Override
            public void run() {

                int jumpTime = 0;
                while (jumpTime < totalProgressTime) {
                    try {
                        sleep(200);
                        jumpTime += 5;
                        progress.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        };
        progresBarTrhead.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }

    private void closeProgressBar() {

        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                progress.hide();
                progresBarTrhead.interrupt();
            }
        });

    }


    public void resetList(int color) {

        this.color = color;
        this.listUidFbUsers = new ArrayList<>();
        listNameFbUsers = new ArrayList<>();
        listHistPerformance = new ArrayList<>();
        listFinalPerformance = new ArrayList<>();
        listHistView = new ArrayList<>();
    }

    public void addUser(String User, String UserName) {
        listHistPerformance.add(new int[24]);
        listFinalPerformance.add(new MyInteger(0));
        listUidFbUsers.add(User);
        listNameFbUsers.add(UserName);
        listHistView.add(null);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        closeProgressBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        GridView gv = (GridView) inflater.inflate(R.layout.list, container, false);
        gv.setBackgroundColor(Color.argb(100, 0, 0, 0));
        myAdapter = new GridAdapter();
        gv.setAdapter(myAdapter);
        mContex = getActivity();
        this.setRetainInstance(true);
        return gv;
    }


    public void initialize(final Activity c) {

        progress = new ProgressDialog(c) {
            @Override
            public void onBackPressed() {
                c.onBackPressed();
            }
        };
        openProgressBar();
        numbOfComparisonDownloaded = 0;
    }

    private class GridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (listUidFbUsers == null)
                return 0;
            else
                return listUidFbUsers.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = new HistGraphView(mContex);
                ((HistGraphView) convertView).resetData(listHistPerformance.get(position), color);
                holder.profilePictureView = (ImageView) convertView.findViewById(R.id.profilepic);
                holder.userName = (TextView) convertView.findViewById(R.id.userName);
                holder.FinalScore = (TextView) convertView.findViewById(R.id.FinalScore);
                convertView.setTag(holder);
            } else {
                ((HistGraphView) convertView).resetData(listHistPerformance.get(position), color);
                holder = (ViewHolder) convertView.getTag();
            }
            if (listHistView.get(position) != null) {
                holder.profilePictureView.setImageDrawable(listHistView.get(position));
                holder.userName.setText(listNameFbUsers.get(position) + getString(R.string.Average));
                holder.FinalScore.setText((int) ((double) listFinalPerformance.get(position).get() / ActiveMilesGUI.TargetMet * 100) + " %");
            }
            return convertView;

        }

    }

    private static class ViewHolder {
        public ImageView profilePictureView;
        public TextView userName;
        public TextView FinalScore;
    }

    @Override
    public void saveImage(int position, int activity, Bitmap drawable) {
        this.listHistView.set(position, getCircProfileImage(drawable));
        this.update();
    }

    private Drawable getCircProfileImage(Bitmap bitmap) {

        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        BitmapShader shader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);
        Canvas c = new Canvas(circleBitmap);
        c.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        return new BitmapDrawable(mContex.getResources(), circleBitmap);
    }
}

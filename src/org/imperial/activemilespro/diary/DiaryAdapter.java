package org.imperial.activemilespro.diary;

import java.util.ArrayList;

import org.imperial.activemilespro.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class DiaryAdapter extends SectionableAdapter implements View.OnClickListener {

    private final ArrayList<String> dates;
    private final ArrayList<Integer> NumberImagesPerDay;
    private final Activity activity;
    private final ArrayList<Uri> filesIcon;
    private final ArrayList<Uri> files;
    private final ArrayList<String> currdataListFormat;

    public DiaryAdapter(Activity activity, LayoutInflater inflater, int rowLayoutID, int headerID, int itemHolderID, int resizeMode, ArrayList<Uri> filesIcon, ArrayList<String> currdataListFormat,
                        ArrayList<Uri> files, ArrayList<String> dates, ArrayList<Integer> NumberImagesPerDay) {

        super(inflater, rowLayoutID, headerID, itemHolderID, resizeMode, NumberImagesPerDay.size());
        this.activity = activity;
        this.filesIcon = filesIcon;
        this.files = files;
        this.dates = dates;
        this.NumberImagesPerDay = NumberImagesPerDay;
        this.currdataListFormat = currdataListFormat;

    }

    @Override
    public Object getItem(int position) {
        for (int i = 0; i < NumberImagesPerDay.size(); ++i) {
            if (position < NumberImagesPerDay.get(i)) {
                return NumberImagesPerDay.get(i);
            }
            position -= NumberImagesPerDay.get(i);
        }
        return null;
    }

    @Override
    protected int getDataCount() {
        int total = 0;
        for (int i = 0; i < NumberImagesPerDay.size(); ++i) {
            total += NumberImagesPerDay.get(i);
        }
        return total;
    }


    @Override
    protected int getCountInSection(int index) {
        return NumberImagesPerDay.get(index);
    }

    @Override
    protected int getTypeFor(int position) {
        int runningTotal = 0;
        for (int i = 0; i < NumberImagesPerDay.size(); ++i) {
            int sectionCount = NumberImagesPerDay.get(i);
            if (position < runningTotal + sectionCount)
                return i;
            runningTotal += sectionCount;
        }
        // This will never happen.
        return -1;
    }

    @Override
    protected String getHeaderForSection(int section) {
        return dates.get(section);
    }

    @Override
    protected void bindView(View convertView, final int position) {

        ImageView picture;
        TextView name;
        picture = (ImageView) convertView.findViewById(R.id.bookItem_title);
        name = (TextView) convertView.findViewById(R.id.text);

        picture.setImageURI(filesIcon.get(position));
        picture.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(activity, FullScreenViewActivity.class);
                i.putExtra("position", position);
                i.putExtra("files", files);
                activity.startActivity(i);
            }
        });

        name.setText(currdataListFormat.get(position));
        convertView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {


    }

}

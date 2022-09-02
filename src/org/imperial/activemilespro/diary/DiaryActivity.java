package org.imperial.activemilespro.diary;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.imperial.activemilespro.R;
import org.imperial.activemilespro.database.PerformanceContentProvider;
import org.imperial.activemilespro.gui.CameraView;
import org.imperial.activemilespro.gui.FacebookManager;
import org.imperial.activemilespro.interface_utility.UtilsCalendar;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class DiaryActivity extends FacebookManager implements LoaderManager.LoaderCallbacks<Cursor> {
    private TextView noImage;
    private ListView list;
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    private final SimpleDateFormat formatter2 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat formatter3 = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final ArrayList<String> dates = new ArrayList<>();
    private final ArrayList<Integer> NumberImagesPerDay = new ArrayList<>();
    private final ArrayList<String> currdataListFormat = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_sectioned_grid);

        noImage = (TextView) findViewById(R.id.NoImage);

        list = (ListView) findViewById(R.id.sectionedGrid_list);

        setBehindContentView(R.layout.conf_diary);

        SlidingMenu sm = getSlidingMenu();
        sm.setSlidingEnabled(true);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindScrollScale(0.25f);
        sm.setFadeDegree(0.25f);
        initializeFacebook();
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri;
        uri = Uri.parse(PerformanceContentProvider.CONTENT_URI + "/" + PerformanceContentProvider.TABLE_IMAGE);

        return (new CursorLoader(this, uri, null, null, null, null));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<Uri> files = new ArrayList<>();
        ArrayList<Uri> filesIcon = new ArrayList<>();
        ArrayList<String> currdataList = new ArrayList<>();
        noImage.setText(getString(R.string.NoImages));
        if (data.moveToFirst()) {
            do {
                File file = new File(CameraView.ImageFileLoc + data.getString(0) + ".jpg");
                File fileIcon = new File(CameraView.IconFileLoc + data.getString(0) + ".jpg");
                if (file.exists() && fileIcon.exists()) {
                    noImage.setText("");
                    Uri uri = Uri.fromFile(file);
                    files.add(uri);
                    uri = Uri.fromFile(fileIcon);
                    filesIcon.add(uri);
                    currdataList.add(UtilsCalendar.timeToStringSec(Long.parseLong(data.getString(0))));
                }
            } while (data.moveToNext());
        }

        String lastday = "";
        String newday;
        Integer currentNumberOfImage = 0;
        for (int i = 0; i < filesIcon.size(); i++) {
            try {
                Date date = formatter.parse(currdataList.get(i));
                currdataListFormat.add(formatter2.format(date));
                newday = formatter3.format(date);
                if (lastday.compareTo(newday) != 0) {
                    if (lastday.compareTo("") != 0)
                        NumberImagesPerDay.add(currentNumberOfImage);
                    lastday = newday;
                    dates.add(newday);
                    currentNumberOfImage = 0;
                }
                currentNumberOfImage++;
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        NumberImagesPerDay.add(currentNumberOfImage);
        DiaryAdapter adapter = new DiaryAdapter(this, getLayoutInflater(), R.layout.diary_row, R.id.bookRow_header, R.id.bookRow_itemHolder, SectionableAdapter.MODE_VARY_WIDTHS, filesIcon,
                currdataListFormat, files, dates, NumberImagesPerDay);
        list.setAdapter(adapter);
        list.setDividerHeight(0);
        list.post(new Runnable() {
            public void run() {
                list.setSelection(list.getCount() - 1);
            }
        });

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}

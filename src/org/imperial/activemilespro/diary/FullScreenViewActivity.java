package org.imperial.activemilespro.diary;

import java.util.ArrayList;

import org.imperial.activemilespro.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class FullScreenViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_view);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);


        Intent i = getIntent();
        int position = i.getIntExtra("position", 0);
        ArrayList<Uri> list =
                (ArrayList<Uri>) i.getSerializableExtra("files");

        FullScreenImageAdapter adapter = new FullScreenImageAdapter(FullScreenViewActivity.this,
                list);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
    }
}

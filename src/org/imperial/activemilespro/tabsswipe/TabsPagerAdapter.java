package org.imperial.activemilespro.tabsswipe;

import java.util.ArrayList;

import org.imperial.activemilespro.gui.ActiveMilesGUI;
import org.imperial.activemilespro.gui.PersonalPerformanceActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public final int[][] dataPerfromance1 = new int[ActiveMilesGUI.NumberOfActivities][24];
    public final int[][] dataPerfromance2 = new int[ActiveMilesGUI.NumberOfActivities][7];
    public final int[][] dataPerfromance3 = new int[ActiveMilesGUI.NumberOfActivities][31];
    public final int[] energyDay = new int[24];
    public final int[] energyWeek = new int[7];
    public final int[] energyMonth = new int[31];
    public final int[] cumulativeActivity = new int[ActiveMilesGUI.NumberOfActivities];
    public final ArrayList<String> timeStemp = new ArrayList<>();
    public final ArrayList<Double> speed = new ArrayList<>();
    public final ArrayList<Double> alt = new ArrayList<>();
    private final DayFragment dayF;
    private final WeekFragment WeekF;
    private final MonthFragment MonthF;
    private final ActivityFragment Actf;
    private final ElevationFragment ElvF;
    private final DayActivityFragment dayActF;
    private final int width;
    private final int height;
    private final String[] tabtitles;

    public TabsPagerAdapter(FragmentManager fm, int w, int h, PersonalPerformanceActivity activity, String[] tabtitles) {
        super(fm);
        this.width = w;
        this.height = h;
        this.tabtitles = tabtitles;
        dayF = new DayFragment();
        dayF.setWebViewFragment("file:///android_asset/chart/Day.html", activity);
        dayF.setRetainInstance(true);
        WeekF = new WeekFragment();
        WeekF.setWebViewFragment("file:///android_asset/chart/Week.html", activity);
        WeekF.setRetainInstance(true);
        MonthF = new MonthFragment();
        MonthF.setWebViewFragment("file:///android_asset/chart/Month.html", activity);
        MonthF.setRetainInstance(true);
        Actf = new ActivityFragment();
        Actf.setWebViewFragment("file:///android_asset/chart/ActivityPie.html", activity);
        Actf.setRetainInstance(true);
        ElvF = new ElevationFragment();
        ElvF.setWebViewFragment("file:///android_asset/chart/Elevation.html", activity);
        ElvF.setRetainInstance(true);
        dayActF = new DayActivityFragment();
        dayActF.setWebViewFragment("file:///android_asset/chart/DayActivity.html", activity);
        dayActF.setRetainInstance(true);
    }

    public void setPerfomanceDay(long time) {
        dayF.setPerfomance(energyDay, time, width, height);
        dayActF.setPerfomance(dataPerfromance1, time, width, height);
    }

    public void setPerfomanceWeek(long time) {
        WeekF.setPerfomance(energyWeek, time, width, height);
    }

    public void setPerfomanceSpeedAlt(long time) {
        Double[] speedA = new Double[speed.size()];
        Double[] AltA = new Double[alt.size()];
        String[] timeStempA = new String[timeStemp.size()];
        speed.toArray(speedA);
        alt.toArray(AltA);
        timeStemp.toArray(timeStempA);
        ElvF.setPerfomance(speedA, AltA, timeStempA, time, width, height);
    }

    public void setPerfomanceMonth(long time) {
        MonthF.setPerfomance(energyMonth, time, width, height);
    }

    public void setPerfomanceAct(long time) {
        Actf.setPerfomance(cumulativeActivity, time, width, height);
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                return dayF;
            case 1:
                return WeekF;
            case 2:
                return MonthF;
            case 3:
                return Actf;
            case 4:
                return ElvF;
            case 5:
                return dayActF;
        }

        return null;
    }

    @Override
    public int getCount() {
        if (ActiveMilesGUI.debug)
            return 6;
        else
            return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabtitles[position];
    }

}

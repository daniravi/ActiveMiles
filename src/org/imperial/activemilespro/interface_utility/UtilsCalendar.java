package org.imperial.activemilespro.interface_utility;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class UtilsCalendar {

    public static long getNumberOfDayInMonth(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static String timeToStringDate(long time) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        sdf.setTimeZone(tz);
        return (sdf.format(new Date(time)));
    }

    public static String timeToStringDateForElevat(long time) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(tz);
        return (sdf.format(new Date(time)));
    }

    public static String timeToStringForView(long time) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        sdf.setTimeZone(tz);
        return (sdf.format(new Date(time)));
    }

    private static String timeToStringHour(long time) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH", Locale.getDefault());
        sdf.setTimeZone(tz);
        return (sdf.format(new Date(time)));
    }

    private static String timeToStringMinute(long time) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault());
        sdf.setTimeZone(tz);
        return (sdf.format(new Date(time)));
    }

    public static String timeToStringSec(long time) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();// get your local time zone.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        sdf.setTimeZone(tz);// set time zone.
        return (sdf.format(new Date(time)));
    }

    public static String currTimeToStringSec() {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();// get your local time zone.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        sdf.setTimeZone(tz);// set time zone.
        return sdf.format(new Date());
    }


    public static String currTimeToStringHour() {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH", Locale.getDefault());
        sdf.setTimeZone(tz);
        return (sdf.format(new Date()));
    }


    public static long addDay(long milliseconds, int dayToadd) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.add(Calendar.DAY_OF_MONTH, dayToadd);
        return cal.getTime().getTime();
    }

    public static String getStartDay(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.getActualMinimum(Calendar.HOUR_OF_DAY), cal.getActualMinimum(Calendar.MINUTE),
                cal.getActualMinimum(Calendar.SECOND));
        return timeToStringHour(cal.getTime().getTime());
    }

    public static long getStartDayInTimeStemp(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.getActualMinimum(Calendar.HOUR_OF_DAY), cal.getActualMinimum(Calendar.MINUTE),
                cal.getActualMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime().getTime();
    }

    public static String getEndDay(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.getActualMaximum(Calendar.HOUR_OF_DAY), cal.getActualMaximum(Calendar.MINUTE),
                cal.getActualMaximum(Calendar.SECOND));
        return timeToStringHour(cal.getTime().getTime());
    }

    public static String getStartDayInMinute(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.getActualMinimum(Calendar.HOUR_OF_DAY), cal.getActualMinimum(Calendar.MINUTE),
                cal.getActualMinimum(Calendar.SECOND));
        return timeToStringMinute(cal.getTime().getTime());
    }

    public static String getStartDayInSec(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.getActualMinimum(Calendar.HOUR_OF_DAY), cal.getActualMinimum(Calendar.MINUTE),
                cal.getActualMinimum(Calendar.SECOND));
        return timeToStringSec(cal.getTime().getTime());
    }

    public static String get08AM_InSec(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.getActualMinimum(Calendar.HOUR_OF_DAY) + 8, cal.getActualMinimum(Calendar.MINUTE),
                cal.getActualMinimum(Calendar.SECOND));
        return timeToStringSec(cal.getTime().getTime());
    }

    public static String get04PM_InSec(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.getActualMinimum(Calendar.HOUR_OF_DAY) + 16, cal.getActualMinimum(Calendar.MINUTE),
                cal.getActualMinimum(Calendar.SECOND));
        return timeToStringSec(cal.getTime().getTime());
    }

    public static String getEndDayInMinute(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.getActualMaximum(Calendar.HOUR_OF_DAY), cal.getActualMaximum(Calendar.MINUTE),
                cal.getActualMaximum(Calendar.SECOND));
        return timeToStringMinute(cal.getTime().getTime());
    }

    public static String getEndDayInSec(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.getActualMaximum(Calendar.HOUR_OF_DAY), cal.getActualMaximum(Calendar.MINUTE),
                cal.getActualMaximum(Calendar.SECOND));
        return timeToStringSec(cal.getTime().getTime());
    }

    public static long computeDiffDay(String day1, String day2) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        sdf.setTimeZone(tz);

        java.util.Date d1 = new java.util.Date();
        java.util.Date d2 = new java.util.Date();
        try {
            d1 = sdf.parse(day1);
            d2 = sdf.parse(day2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (d1.getTime() - d2.getTime()) / (1000 * 60 * 60 * 24);

    }

    public static long computeDiffSec(String day1, String day2) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        sdf.setTimeZone(tz);

        java.util.Date d1 = new java.util.Date();
        java.util.Date d2 = new java.util.Date();
        try {
            d1 = sdf.parse(day1);
            d2 = sdf.parse(day2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (d1.getTime() - d2.getTime()) / (1000);

    }

    public static String getStartWeek(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.getActualMinimum(Calendar.HOUR_OF_DAY), cal.getActualMinimum(Calendar.MINUTE),
                cal.getActualMinimum(Calendar.SECOND));
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        return timeToStringDate(cal.getTime().getTime());
    }

    public static long getStartWeekInTimeStemp(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.getActualMinimum(Calendar.HOUR_OF_DAY), cal.getActualMinimum(Calendar.MINUTE),
                cal.getActualMinimum(Calendar.SECOND));
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime().getTime();
    }

    public static String getEndWeek(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.getActualMaximum(Calendar.HOUR_OF_DAY), cal.getActualMaximum(Calendar.MINUTE),
                cal.getActualMaximum(Calendar.SECOND));
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        return timeToStringDate(cal.getTime().getTime());
    }

    public static String getStartMonth(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.getActualMinimum(Calendar.DAY_OF_MONTH), cal.getActualMinimum(Calendar.HOUR_OF_DAY), cal.getActualMinimum(Calendar.MINUTE),
                cal.getActualMinimum(Calendar.SECOND));
        // cal.add(Calendar.DAY_OF_MONTH, 1);
        return timeToStringDate(cal.getTime().getTime());
    }

    public static long getStartMonthinTimeStemp(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.getActualMinimum(Calendar.DAY_OF_MONTH), cal.getActualMinimum(Calendar.HOUR_OF_DAY), cal.getActualMinimum(Calendar.MINUTE),
                cal.getActualMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime().getTime();
    }

    public static String getEndMonth(long milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.getActualMaximum(Calendar.DAY_OF_MONTH), cal.getActualMaximum(Calendar.HOUR_OF_DAY), cal.getActualMaximum(Calendar.MINUTE),
                cal.getActualMaximum(Calendar.SECOND));
        // cal.add(Calendar.DAY_OF_MONTH, 1);
        return timeToStringDate(cal.getTime().getTime());
    }

    public static long TimeToTimestamp(int year, int month, int day, int hour, int min) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.MINUTE, min);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.HOUR, hour);
        c.set(Calendar.AM_PM, 0);
        return (c.getTime().getTime());
    }

    public static String TimeToTimeUTC_FB(int year, int month, int day, int hour, int min) {
        long tmpTime = TimeToTimestamp(year, month, day, hour, min);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(tmpTime));
    }
}

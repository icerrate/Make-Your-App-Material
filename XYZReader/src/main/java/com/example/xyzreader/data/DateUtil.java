package com.example.xyzreader.data;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Ivan Cerrate.
 */
public class DateUtil {

    private static final String TAG = DateUtil.class.getSimpleName();

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private static SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    public static GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    public static Date parseDate(String StringDate) {
        try {
            return dateFormat.parse(StringDate);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    public static String parseStringDate(Date date) {
        return outputFormat.format(date);
    }
}

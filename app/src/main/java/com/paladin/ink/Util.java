package com.paladin.ink;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.nio.ByteBuffer;
import java.util.Calendar;

/**
 * Created by jason on 11/5/16.
 */
public class Util {

    public static byte[] bitmapToBytes(Bitmap b) {
        //calculate how many bytes our image consists of.
        int bytes = b.getByteCount();
//or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
//int bytes = b.getWidth()*b.getHeight()*4;

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
        b.copyPixelsToBuffer(buffer); //Move the byte data to the buffer

        byte[] array = buffer.array();

        return array;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public static String getDateString() {
        String stringDate = "";
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int month = calendar.get(Calendar.MONTH);
        int dayInt = calendar.get(Calendar.DAY_OF_MONTH);
        switch (day) {
            case Calendar.SUNDAY:
                stringDate += "Sunday";
                break;

            case Calendar.MONDAY:
                stringDate += "Monday";
                break;

            case Calendar.TUESDAY:
                stringDate += "Tuesday";
                break;

            case Calendar.WEDNESDAY:
                stringDate += "Wednesday";
                break;

            case Calendar.THURSDAY:
                stringDate += "Thursday";
                break;

            case Calendar.FRIDAY:
                stringDate += "Friday";
                break;

            case Calendar.SATURDAY:
                stringDate += "Saturday";
                break;

        }
        switch (month) {
            case Calendar.JANUARY:
                stringDate += ", January";
                break;

            case Calendar.FEBRUARY:
                stringDate += ", February";
                break;

            case Calendar.MARCH:
                stringDate += ", March";
                break;

            case Calendar.APRIL:
                stringDate += ", April";
                break;

            case Calendar.MAY:
                stringDate += ", May";
                break;

            case Calendar.JUNE:
                stringDate += ", June";
                break;

            case Calendar.JULY:
                stringDate += ", July";
                break;

            case Calendar.AUGUST:
                stringDate += ", August";
                break;

            case Calendar.SEPTEMBER:
                stringDate += ", September";
                break;

            case Calendar.OCTOBER:
                stringDate += ", October";
                break;

            case Calendar.NOVEMBER:
                stringDate += ", November";
                break;

            case Calendar.DECEMBER:
                stringDate += ", December";
                break;

        }


        stringDate += (" " + dayInt);

        return stringDate;
    }
}

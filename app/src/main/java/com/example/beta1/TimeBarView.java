package com.example.beta1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.1
 * @since 1 /2/2023 A custom View that is used in the HourSelect Activity.
 * displays the time ranges at which a given ParkAd is available, and the time ranges at which its not.
 */

public class TimeBarView extends View {

    /**
     * The value of the number at the top of the TimeBar View.
     */
    private float mMaxValue;

    /**
     * The value of the value at the bottom of the TimeBar View.
     */
    private float mMinValue;

    /**
     * The width of the TimeBar View.
     */
    private float mBarWidth = 70;

    /**
     * The Paint Object used to create the TimeBar View.
     */
    private Paint mPaint;

    /**
     * List of Segments to be added to the TimeBar View.
     */
    public List<Segment> mSegments = new ArrayList<>();

    /**
     * Instantiates a new TimeBar View.
     *
     * @param context the context
     */
    public TimeBarView(Context context) {
        super(context);
        init();
    }

    /**
     * Instantiates a new TimeBar View.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public TimeBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Instantiates a new TimeBar View
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public TimeBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLUE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();

        canvas.drawRect(0, 0, width, height, mPaint);
        Paint segPaint = new Paint();
        segPaint.setColor(Color.RED);
        Paint textPaint = new Paint();
        textPaint.setTextSize(mBarWidth / 2);
        textPaint.setColor(Color.BLACK);


        for (Segment segment : mSegments) {
            float startY = convertValueToYCoordinate(segment.getDoubleFromTimeString(segment.getBeginHour()));
            float endY = convertValueToYCoordinate(segment.getDoubleFromTimeString(segment.getEndHour()));
            canvas.drawRect(0, startY, getWidth(), endY, segPaint);
            float startTextPos = startY + (mBarWidth / 2);
            canvas.drawText(segment.getBeginHour(), 0, startTextPos, textPaint);

            float endTextPos = endY + (mBarWidth / 2) - 50;
            canvas.drawText(segment.getEndHour(), 0, endTextPos, textPaint);
        }


    }

    /**
     * SubMethod for the onDraw Method.
     * Used to convert the time values of the begin and end hours of a given ParkAd to Y values,
     * for the TimeBar View.
     *
     * @param value
     * @return
     */
    private float convertValueToYCoordinate(double value) {
        float height = getHeight();
        float range = mMaxValue - mMinValue;
        float ratio = (float) (value - mMinValue) / range;
        return height * (1 - ratio);
    }


    /**
     * Sets top number.
     *
     * @param topNumber the top number
     */
    public void setTopNumber(double topNumber) {
        mMaxValue = (float) topNumber;
        invalidate();
    }

    /**
     * Sets bottom number.
     *
     * @param bottomNumber the bottom number
     */
    public void setBottomNumber(double bottomNumber) {
        mMinValue = (float) bottomNumber;
        invalidate();
    }

    /**
     * The Segment Class.
     * Used to segment the TimeBar View into available and unavailable time ranges.
     * Each Segment is colored red, and has a begin hour and end hour to it.
     */
    public static class Segment {

        /**
         * The End hour.
         */
        public String endHour;
        /**
         * The Begin hour.
         */
        public String beginHour;

        /**
         * Instantiates a new Segment.
         *
         * @param beginH the begin hour
         * @param endH   the end hour
         */
        public Segment(String beginH, String endH) {

            endHour = endH;
            beginHour = beginH;
        }


        /**
         * Gets end hour.
         *
         * @return the end hour
         */
        public String getEndHour() {
            return endHour;
        }

        /**
         * Gets begin hour.
         *
         * @return the begin hour
         */
        public String getBeginHour() {
            return beginHour;
        }

        /**
         * Gets double from time string.
         *
         * @param timeStr the time str ('HH:mm' format)
         * @return: The Method returns a double value from the time string (e.g the input '16:45'
         * will return 16.45.
         */
        public double getDoubleFromTimeString(String timeStr) {
            String[] timeComponents = timeStr.split(":");
            int hour = Integer.parseInt(timeComponents[0]);
            int minute = Integer.parseInt(timeComponents[1]);
            double minuteFactor = 0;
            switch (minute) {
                case 0:
                    break;
                case 15:
                    minuteFactor = 0.25;
                    break;
                case 30:
                    minuteFactor = 0.5;
                    break;
                case 45:
                    minuteFactor = 0.75;
                    break;
                default:
                    minuteFactor = 0;
            }
            return hour + minuteFactor;
        }
    }

    /**
     * Adds a segment to the TimeBar View.
     *
     * @param segment the segment
     */
    public void addSegment(Segment segment) {
        mSegments.add(segment);
        invalidate();
    }

}


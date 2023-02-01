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

public class TimeBarView extends View {
    private float mMaxValue;
    private float mMinValue;
    private float mBarWidth = 70;
    private Paint mPaint;
    public List<Segment> mSegments = new ArrayList<>();

    public TimeBarView(Context context) {
        super(context);
        init();
    }

    public TimeBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

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

    private float convertValueToYCoordinate(double value) {
        float height = getHeight();
        float range = mMaxValue - mMinValue;
        float ratio = (float) (value - mMinValue) / range;
        return height * (1 - ratio);
    }


    public void addSegment(Segment segment) {
        mSegments.add(segment);
        invalidate();
    }

    public void setTopNumber(double topNumber) {
        mMaxValue = (float) topNumber;
        invalidate();
    }

    public void setBottomNumber(double bottomNumber) {
        mMinValue = (float) bottomNumber;
        invalidate();
    }

    public static class Segment {

        public String endHour;
        public String beginHour;

        public Segment( String beginH, String endH) {

            endHour = endH;
            beginHour = beginH;
        }


        public String getEndHour() {
            return endHour;
        }

        public String getBeginHour() {
            return beginHour;
        }

        public double getDoubleFromTimeString(String timeStr) {
            String[] timeComponents = timeStr.split(":");
            int hour = Integer.parseInt(timeComponents[0]);
            int minute = Integer.parseInt(timeComponents[1]);
            double minuteFactor = 0;
            switch (minute) {
                case 0:
                    minuteFactor = 0;
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

}


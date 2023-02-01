package com.example.beta1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

public class TimeBarView extends View {
    private float mMaxValue = 0;
    private float mMinValue = 24;
    private float mBarWidth = (float) 100;
    private Paint mPaint;
    public List<Segment> mSegments = new ArrayList<>();

    public TimeBarView(Context context) {
        super(context);
    }

    public TimeBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLUE);
    }

    public TimeBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();

        canvas.drawRect(0, 0, width, height, mPaint);
        mPaint.setColor(Color.RED);
        for (Segment segment : mSegments) {
            float startY = convertValueToYCoordinate(segment.start);
            float endY = convertValueToYCoordinate(segment.end);
            canvas.drawRect(0, startY, getWidth(), endY, mPaint);

            Paint textPaint = new Paint();
            textPaint.setTextSize(mBarWidth / 2);
            textPaint.setColor(Color.BLACK);

            float startTextPos = startY + (mBarWidth / 2);
            canvas.drawText(segment.getBeginHour(), 0, startTextPos, textPaint);

            float endTextPos = endY + (mBarWidth / 2) - 50;
            canvas.drawText(segment.getEndHour(), 0, endTextPos, textPaint);

        }
    }

    private float convertValueToYCoordinate(float value) {
        float height = getHeight();
        float range = mMaxValue - mMinValue;
        float ratio = (value - mMinValue) / range;
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
        public float start;
        public float end;
        public String endHour;
        public String beginHour;
        public Paint paint = new Paint();

        public Segment(double start, double end, int color, String beginH, String endH) {
            this.start = (float) start;
            this.end = (float) end;
            paint.setColor(color);
            endHour = endH;
            beginHour = beginH;
        }


        public String getEndHour() {
            return endHour;
        }

        public String getBeginHour() {
            return beginHour;
        }

        private String hoursToText(float param) {
            String time = "";
            String hour, minutes;
            float fraction = param - (float) Math.floor(param);
            hour = String.valueOf((int) param);
            minutes = String.valueOf((int) (fraction * 60));
            if (Integer.valueOf(hour) < 10) hour = "0" + hour;
            if (Integer.valueOf(minutes) < 10) minutes = "0" + minutes;
            time = hour + ":" + minutes;

            return time;
        }

    }
}

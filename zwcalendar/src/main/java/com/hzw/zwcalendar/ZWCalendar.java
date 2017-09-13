package com.hzw.zwcalendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * 功能：
 * Created by 何志伟 on 2017/8/15.
 */

class ZWCalendar extends View {

    private String[] weekTitles = new String[]{"日", "一", "二", "三", "四", "五", "六"};
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private Calendar calendar = Calendar.getInstance();
    //今天所在的年月日信息
    private int currentYear, currentMonth, currentDay;
    //点击选中的年月日信息
    private int clickYear, clickMonth, clickDay;
    //当前选择的年月信息
    private int selectYear, selectMonth;
    private GregorianCalendar date = new GregorianCalendar();
    private HashMap<String, Boolean> signRecords;
    private GestureDetectorCompat detectorCompat;
    private Bitmap signSuccess, signError;
    private ZWCalendarView.Config config;
    private Paint paint = new Paint();
    private LunarHelper lunarHelper;
    private int itemWidth, itemHeight;
    private float solarTextHeight;
    private int currentPosition;
    private float signDelay;


    public ZWCalendar(Context context) {
        super(context);
    }

    public ZWCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZWCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void init(ZWCalendarView.Config config) {
        this.config = config;
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        selectYear = currentYear;
        selectMonth = currentMonth;
        clickYear = currentYear;
        clickMonth = currentMonth;
        clickDay = currentDay;
        detectorCompat = new GestureDetectorCompat(getContext(), gestureListener);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStrokeWidth(sp2px(0.6f));
        currentPosition = (currentYear - 1970) * 12 + currentMonth + 1;
        setClickable(true);
        if (config.isShowLunar) lunarHelper = new LunarHelper();
        if (config.signIconSuccessId != 0) {
            signSuccess = BitmapFactory.decodeResource(getResources(), config.signIconSuccessId);
            signError = BitmapFactory.decodeResource(getResources(), config.signIconErrorId);
            if (signSuccess != null) {
                int width = signSuccess.getWidth();
                int height = signSuccess.getHeight();
                Matrix matrix = new Matrix();
                matrix.postScale(config.signIconSize / width, config.signIconSize / height);
                signSuccess = Bitmap.createBitmap(signSuccess, 0, 0, width, height, matrix, true);
                signError = Bitmap.createBitmap(signError, 0, 0, width, height, matrix, true);
            }
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        itemWidth = getWidth() / 7;
        itemHeight = (getHeight() - (int) config.weekHeight) / 6;
        paint.setTextSize(config.calendarTextSize);
        solarTextHeight = getTextHeight();
        signDelay = getX(Math.min(itemHeight, itemWidth) / 2, -45);
    }

    final void selectDate(int position) {
        currentPosition = position - 1;
        selectYear = 1970 + currentPosition / 12;
        selectMonth = currentPosition % 12;
        invalidate();
    }

    final void initSelect(int clickYear, int clickMonth, int clickDay) {
        this.clickYear = clickYear;
        this.clickMonth = clickMonth;
        this.clickDay = clickDay;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(Color.LTGRAY);
        canvas.drawLine(0, config.weekHeight, 0, getHeight() - config.weekHeight, paint);
        //画日历顶部周的标题
        paint.setColor(config.weekBackgroundColor);
        canvas.drawRect(0, 0, getWidth(), config.weekHeight, paint);
        paint.setTextSize(config.weekTextSize);
        paint.setColor(config.weekTextColor);
        float delay = getTextHeight() / 4;
        for (int i = 0; i < 7; i++) {
            canvas.drawText(weekTitles[i], itemWidth * (i + 0.5f), config.weekHeight / 2 + delay, paint);
        }
        //画日历
        int year = 1970 + currentPosition / 12;
        int month = currentPosition % 12;
        calendar.set(year, month, 1);
        int firstDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int selectMonthMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        //上一个月的最大天数
        calendar.add(Calendar.MONTH, -1);
        int previousMonthMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        delay = solarTextHeight / 4;
        if (config.isShowLunar) delay = 0;
        for (int i = 1; i <= 42; i++) {
            int copyI = i - 1;
            int x = (copyI % 7) * itemWidth + itemWidth / 2;
            int y = (copyI / 7) * itemHeight + itemHeight / 2 + (int) config.weekHeight + (int) delay;
            if (i <= firstDay) {//前一月数据
                if (!config.isShowOtherMonth) continue;
                int day = previousMonthMaxDay - firstDay + i;
                paint.setColor(config.otherMonthTextColor);
                paint.setTextSize(config.calendarTextSize);
                canvas.drawText(String.valueOf(day), x, y, paint);
                drawLunar(canvas, month == 0 ? (year - 1) : year, month == 0 ? 11 : (month - 1), day, x, y);
            } else if (i > selectMonthMaxDay + firstDay) {//后一月数据
                if (!config.isShowOtherMonth) continue;
                int day = i - firstDay - selectMonthMaxDay;
                paint.setColor(config.otherMonthTextColor);
                paint.setTextSize(config.calendarTextSize);
                canvas.drawText(String.valueOf(day), x, y, paint);
                drawLunar(canvas, month == 11 ? (year + 1) : year, month == 11 ? 0 : (month + 1), day, x, y);
            } else {//当前月数据
                int day = i - firstDay;
                if (year == currentYear && month == currentMonth && day == currentDay) {//今天
                    paint.setColor(config.todayTextColor);
                } else {//其他天
                    paint.setColor(config.calendarTextColor);
                }
                if (year == clickYear && month == clickMonth && day == clickDay) {//当前选中的一天
                    paint.setColor(config.selectColor);
                    canvas.drawCircle(x, y - delay, Math.min(itemHeight, itemWidth) / 2, paint);
                    paint.setColor(config.selectTextColor);
                }
                paint.setTextSize(config.calendarTextSize);
                drawSign(canvas, year, month, day, x, y);
                canvas.drawText(String.valueOf(day), x, y, paint);
                drawLunar(canvas, year, month, day, x, y);
            }
        }
    }


    private void drawLunar(Canvas canvas, int year, int month, int day, int x, int y) {
        if (config.isShowLunar) {
            if (year != clickYear || month != clickMonth || day != clickDay) {
                paint.setColor(config.lunarTextColor);
            }
            String lunar = lunarHelper.SolarToLunarString(year, month + 1, day);
            paint.setTextSize(config.lunarTextSize);
            canvas.drawText(lunar, x, y + solarTextHeight * 2 / 3, paint);
        }
    }

    private void drawSign(Canvas canvas, int year, int month, int day, int x, int y) {
        if (signSuccess == null || signRecords == null) return;
        date.set(year, month, day);
        String dateStr = format.format(date.getTime());
        if (signRecords.containsKey(dateStr)) {
            if (year != clickYear || month != clickMonth || day != clickDay) {
                paint.setColor(config.signTextColor);
            }
            if (signRecords.get(dateStr)) {
                canvas.drawBitmap(signSuccess, x + signDelay - config.signIconSize / 2,
                        y - signDelay - config.signIconSize / 2, paint);
            } else {
                canvas.drawBitmap(signError, x + signDelay - config.signIconSize / 2,
                        y - signDelay - config.signIconSize / 2, paint);
            }
        }
    }

    int[] getCurrentDayInfo() {
        return new int[]{currentYear, currentMonth, currentDay};
    }

    private float getX(float radius, int angle) {
        int centerX = 0;
        return (float) (centerX + radius * Math.cos(angle * 3.14 / 180));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private GestureDetector.OnGestureListener
            gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int position = getPosition(e.getX(), e.getY());
            if (dateClickListener != null) {
                calendar.set(selectYear, selectMonth, 1);
                int firstDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                calendar.set(selectYear, selectMonth, position - firstDay + 1);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                if (month == selectMonth) {
                    clickYear = year;
                    clickMonth = month;
                    clickDay = day;
                    invalidate();
                    dateClickListener.dateSelect(year, month, day, week);
                }
            }
            return super.onSingleTapUp(e);
        }
    };

    private int getPosition(float x, float y) {
        y -= config.weekHeight;
        int y1 = (int) (y / itemHeight);
        int x1 = (int) (x / itemWidth);
        return y1 * 7 + x1;
    }

    void setSignRecords(HashMap<String, Boolean> signRecords) {
        this.signRecords = signRecords;
    }

    private float sp2px(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return spValue * fontScale + 0.5f;
    }

    private float getTextHeight() {
        return paint.descent() - paint.ascent();
    }

    interface DateSelectListener {
        void dateSelect(int year, int month, int day, int week);
    }

    private DateSelectListener dateClickListener;

    void setDateSelectListener(DateSelectListener clickListener) {
        dateClickListener = clickListener;
    }


}

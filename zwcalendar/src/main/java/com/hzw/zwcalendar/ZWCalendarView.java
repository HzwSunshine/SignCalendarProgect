package com.hzw.zwcalendar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.util.ArraySet;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.Calendar;
import java.util.HashMap;


/**
 * 功能：签到日历
 * Created by 何志伟 on 2017/8/25.
 */
public class ZWCalendarView extends FrameLayout {

    private ArraySet<ZWCalendar> destroyViews = new ArraySet<>();
    private ArraySet<ZWCalendar> viewSet = new ArraySet<>();
    private int selectYear, selectMonth, selectDay, selectWeek;
    private HashMap<String, Boolean> signRecords;
    private int PAGER_SIZE = 1200;
    private Calendar calendar;
    private ViewPager pager;
    private Config config;

    public ZWCalendarView(Context context) {
        super(context);
        init(null, 0);
    }

    public ZWCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ZWCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int def) {
        config = new Config();
        //获取日历的UI配置
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ZWCalendarView, def, 0);
        config.weekHeight = array.getDimension(R.styleable.ZWCalendarView_weekHeight, dip2px(30));
        config.weekBackgroundColor = array.getColor(R.styleable.ZWCalendarView_weekBackgroundColor, Color.WHITE);
        config.weekTextColor = array.getColor(R.styleable.ZWCalendarView_weekTextColor, Color.GRAY);
        config.weekTextSize = array.getDimension(R.styleable.ZWCalendarView_weekTextSize, sp2px(14));
        config.calendarTextColor = array.getColor(R.styleable.ZWCalendarView_calendarTextColor, Color.BLACK);
        config.calendarTextSize = array.getDimension(R.styleable.ZWCalendarView_calendarTextSize, sp2px(14));
        config.isShowOtherMonth = array.getBoolean(R.styleable.ZWCalendarView_isShowOtherMonth, false);
        if (config.isShowOtherMonth) {
            config.otherMonthTextColor = array.getColor(R.styleable.ZWCalendarView_otherMonthTextColor, Color.LTGRAY);
        }
        config.isShowLunar = array.getBoolean(R.styleable.ZWCalendarView_isShowLunar, false);
        if (config.isShowLunar) {
            config.lunarTextColor = array.getColor(R.styleable.ZWCalendarView_lunarTextColor, Color.LTGRAY);
            config.lunarTextSize = array.getDimension(R.styleable.ZWCalendarView_lunarTextSize, sp2px(11));
        }
        config.todayTextColor = array.getColor(R.styleable.ZWCalendarView_todayTextColor, Color.BLUE);
        config.selectColor = array.getColor(R.styleable.ZWCalendarView_selectColor, Color.BLUE);
        config.selectTextColor = array.getColor(R.styleable.ZWCalendarView_selectTextColor, Color.WHITE);
        config.signIconSuccessId = array.getResourceId(R.styleable.ZWCalendarView_signIconSuccessId, 0);
        config.signIconErrorId = array.getResourceId(R.styleable.ZWCalendarView_signIconErrorId, 0);
        if (config.signIconSuccessId != 0) {
            config.signIconSize = array.getDimension(R.styleable.ZWCalendarView_signIconSize, dip2px(16));
        }
        config.signTextColor = array.getColor(R.styleable.ZWCalendarView_signTextColor, Color.parseColor("#BA7436"));
        config.limitFutureMonth = array.getBoolean(R.styleable.ZWCalendarView_limitFutureMonth, false);
        array.recycle();
        initPager();
    }

    private float sp2px(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return spValue * fontScale + 0.5f;
    }

    private int dip2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        height = hMode == MeasureSpec.EXACTLY ? height : dip2px(220);
        setMeasuredDimension(widthMeasureSpec, height);
    }

    private void initPager() {
        pager = new ViewPager(getContext());
        addView(pager);
        pager.setOffscreenPageLimit(1);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (listener == null) return;
                position -= 1;
                int year = 1970 + position / 12;
                int month = position % 12;
                listener.change(year, month + 1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //选择当前日期
        calendar = Calendar.getInstance();
        selectYear = calendar.get(Calendar.YEAR);
        selectMonth = calendar.get(Calendar.MONTH);
        selectDay = calendar.get(Calendar.DAY_OF_MONTH);
        selectWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int currentPosition = getPosition(selectYear, selectMonth);
        if (config.limitFutureMonth) PAGER_SIZE = currentPosition + 1;
        pager.setAdapter(new CVAdapter());
        pager.setCurrentItem(currentPosition, false);
    }

    private int getPosition(int year, int month) {
        return (year - 1970) * 12 + month + 1;
    }

    private class CVAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return PAGER_SIZE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ZWCalendar calendarView = getContent(position);
            container.addView(calendarView);
            return calendarView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            destroyViews.add((ZWCalendar) object);
            container.removeView((View) object);
        }
    }

    private ZWCalendar getContent(int position) {
        ZWCalendar calendarView;
        if (destroyViews.size() != 0) {
            calendarView = destroyViews.valueAt(0);
            destroyViews.remove(calendarView);
        } else {
            calendarView = new ZWCalendar(getContext());
            calendarView.init(config);
            calendarView.setDateSelectListener(new ZWCalendar.DateSelectListener() {
                @Override
                public void dateSelect(int year, int month, int day, int week) {
                    selectYear = year;
                    selectMonth = month;
                    selectDay = day;
                    for (ZWCalendar view : viewSet) {
                        view.initSelect(selectYear, selectMonth, selectDay);
                    }
                    if (listener != null) {
                        listener.select(year, month + 1, day, week == 0 ? 7 : week);
                    }
                }
            });
            viewSet.add(calendarView);
        }
        calendarView.setSignRecords(signRecords);
        calendarView.selectDate(position);
        calendarView.setTag(position);
        return calendarView;
    }

    static class Config {
        float weekHeight;
        float weekTextSize;
        int weekBackgroundColor;
        int weekTextColor;
        float calendarTextSize;
        int calendarTextColor;
        boolean isShowOtherMonth;
        int otherMonthTextColor;
        boolean isShowLunar;
        int lunarTextColor;
        float lunarTextSize;
        int todayTextColor;
        int selectColor;
        int selectTextColor;
        int signIconSuccessId;
        int signIconErrorId;
        float signIconSize;
        int signTextColor;
        boolean limitFutureMonth;
    }

    /*---------------------------------------对外方法-----------------------------------*/

    public interface SelectListener {
        void change(int year, int month);

        void select(int year, int month, int day, int week);
    }

    private SelectListener listener;

    public void setSelectListener(SelectListener listener) {
        this.listener = listener;
        listener.change(selectYear, selectMonth + 1);
        selectWeek = selectWeek == 0 ? 7 : selectWeek;
        listener.select(selectYear, selectMonth + 1, selectDay, selectWeek);
    }


    public void selectDate(int year, int month, int day) {
        if (listener == null || year < 1970 || month < 1 || month > 12 || day < 1 || day > 31)
            return;
        month -= 1;
        calendar.set(year, month, day);
        int yearTemp = calendar.get(Calendar.YEAR);
        int monthTemp = calendar.get(Calendar.MONTH);
        int dayTemp = calendar.get(Calendar.DAY_OF_MONTH);
        if (yearTemp != year || monthTemp != month || dayTemp != day) return;
        selectYear = year;
        selectMonth = month;
        selectDay = day;
        for (ZWCalendar view : viewSet) {
            view.initSelect(selectYear, selectMonth, selectDay);
        }
        int position = getPosition(selectYear, selectMonth);
        pager.setCurrentItem(position, false);
        int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        listener.select(selectYear, selectMonth + 1, selectDay, week == 0 ? 7 : week);
    }

    public void selectMonth(int year, int month) {
        if (pager == null || year < 1970 || month < 1 || month > 12) return;
        int position = getPosition(year, month);
        pager.setCurrentItem(position, false);
    }

    /**
     * 设置签到记录
     *
     * @param signRecords 为日期格式像 "2017-08-25"
     */
    public void setSignRecords(final HashMap<String, Boolean> signRecords) {
        if (signRecords == null) return;
        this.signRecords = signRecords;
        for (ZWCalendar view : viewSet) {
            view.setSignRecords(signRecords);
            view.invalidate();
        }
    }

    public void showNextMonth() {
        if (pager != null) {
            int index = pager.getCurrentItem() + 1;
            pager.setCurrentItem(index, true);
        }
    }

    public void showPreviousMonth() {
        if (pager != null) {
            int index = pager.getCurrentItem() - 1;
            pager.setCurrentItem(index, true);
        }
    }

    public void backToday() {
        if (pager != null) {
            ZWCalendar item = (ZWCalendar) pager.findViewWithTag(pager.getCurrentItem());
            if (item != null) {
                int[] info = item.getCurrentDayInfo();
                selectDate(info[0], info[1] + 1, info[2]);
            }
        }
    }


}

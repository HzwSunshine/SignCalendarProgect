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
import java.util.HashSet;


/**
 * 功能：签到日历
 * Created by 何志伟 on 2017/8/25.
 */
public class ZWSignCalendarView extends FrameLayout {

    private ArraySet<ZWSignCalendar> destroyViews = new ArraySet<>();
    private ArraySet<ZWSignCalendar> viewSet = new ArraySet<>();
    private HashSet<String> signRecords;
    private int PAGER_SIZE = 1200;
    private ViewPager pager;
    private Config config;
    private int currentPosition;

    public ZWSignCalendarView(Context context) {
        super(context);
        init(null, 0);
    }

    public ZWSignCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ZWSignCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int def) {
        config = new Config();
        //获取日历的UI配置
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ZWSignCalendarView, def, 0);
        config.weekHeight = array.getDimension(R.styleable.ZWSignCalendarView_weekHeight, dip2px(30));
        config.weekBackgroundColor = array.getColor(R.styleable.ZWSignCalendarView_weekBackgroundColor, Color.WHITE);
        config.weekTextColor = array.getColor(R.styleable.ZWSignCalendarView_weekTextColor, Color.GRAY);
        config.weekTextSize = array.getDimension(R.styleable.ZWSignCalendarView_weekTextSize, sp2px(14));
        config.calendarTextColor = array.getColor(R.styleable.ZWSignCalendarView_calendarTextColor, Color.BLACK);
        config.calendarTextSize = array.getDimension(R.styleable.ZWSignCalendarView_calendarTextSize, sp2px(14));
        config.isShowLunar = array.getBoolean(R.styleable.ZWSignCalendarView_isShowLunar, false);
        if (config.isShowLunar) {
            config.lunarTextColor = array.getColor(R.styleable.ZWSignCalendarView_lunarTextColor, Color.LTGRAY);
            config.lunarTextSize = array.getDimension(R.styleable.ZWSignCalendarView_lunarTextSize, sp2px(11));
        }
        config.todayTextColor = array.getColor(R.styleable.ZWSignCalendarView_todayTextColor, Color.BLUE);
        config.signSize = array.getDimension(R.styleable.ZWSignCalendarView_signSize, 0);
        config.signTextColor = array.getColor(R.styleable.ZWSignCalendarView_signTextColor, Color.parseColor("#BA7436"));
        config.limitFutureMonth = array.getBoolean(R.styleable.ZWSignCalendarView_limitFutureMonth, false);
        config.signColor = array.getColor(R.styleable.ZWSignCalendarView_signColor, Color.BLUE);
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
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        currentPosition = getPosition(year, month);
        if (config.limitFutureMonth) PAGER_SIZE = currentPosition + 1;
        pager.setAdapter(new CVAdapter());
        pager.setCurrentItem(currentPosition, false);
        post(new Runnable() {
            public void run() {
                if (listener != null) listener.change(year, month + 1);
            }
        });
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
            ZWSignCalendar calendarView = getContent(position);
            container.addView(calendarView);
            return calendarView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            destroyViews.add((ZWSignCalendar) object);
            container.removeView((View) object);
        }
    }

    private ZWSignCalendar getContent(int position) {
        ZWSignCalendar calendarView;
        if (destroyViews.size() != 0) {
            calendarView = destroyViews.valueAt(0);
            destroyViews.remove(calendarView);
        } else {
            calendarView = new ZWSignCalendar(getContext());
            calendarView.init(config);
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
        boolean isShowLunar;
        int lunarTextColor;
        float lunarTextSize;
        int todayTextColor;
        int signColor;
        float signSize;
        int signTextColor;
        boolean limitFutureMonth;
    }

    /*---------------------------------------对外方法-----------------------------------*/

    public interface DateListener {
        void change(int year, int month);
    }

    private DateListener listener;

    public void setDateListener(DateListener listener) {
        this.listener = listener;
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
    public void setSignRecords(final HashSet<String> signRecords) {
        if (signRecords == null) return;
        this.signRecords = signRecords;
        for (ZWSignCalendar view : viewSet) {
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

    public void backCurrentMonth() {
        if (pager != null) {
            pager.setCurrentItem(currentPosition, false);
        }
    }


}

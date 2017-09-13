package com.hzw.signcalendarprogect;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hzw.zwcalendar.ZWCalendarView;
import com.hzw.zwcalendar.ZWSignCalendarView;

import java.util.HashMap;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private ZWCalendarView calendarView;
    private ZWSignCalendarView signCalendarView;
    private TextView show, show2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = (ZWCalendarView) findViewById(R.id.calendarView);
        show = (TextView) findViewById(R.id.tv_calendar_show);
        show2 = (TextView) findViewById(R.id.tv_calendar_show2);
        calendarView.setSelectListener(new ZWCalendarView.SelectListener() {
            @Override
            public void change(int year, int month) {
                show.setText(String.format("%s 年 %s 月", year, month));
            }

            @Override
            public void select(int year, int month, int day, int week) {
                Toast.makeText(getApplicationContext(),
                        String.format("%s 年 %s 月 %s日，周%s", year, month, day, week),
                        Toast.LENGTH_SHORT).show();
            }
        });

        //代码选中一个日期
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.selectDate(2017, 9, 3);
            }
        });

        //前一月
        findViewById(R.id.calendar_previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.showPreviousMonth();
            }
        });

        //后一月
        findViewById(R.id.calendar_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.showNextMonth();
            }
        });

        //返回今天
        findViewById(R.id.tv_calendar_today).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.backToday();
            }
        });

        HashMap<String, Boolean> sign = new HashMap<>();
        sign.put("2017-07-12", true);
        sign.put("2017-07-23", true);
        sign.put("2017-07-24", false);
        sign.put("2017-07-25", true);
        sign.put("2017-08-12", false);
        sign.put("2017-08-13", true);
        sign.put("2017-08-14", true);
        sign.put("2017-08-15", false);
        sign.put("2017-08-18", false);
        sign.put("2017-08-31", true);
        sign.put("2017-09-05", true);
        sign.put("2017-09-07", false);
        sign.put("2017-09-08", false);
        sign.put("2017-09-09", true);
        sign.put("2017-10-09", true);
        calendarView.setSignRecords(sign);


        //
        //
        /*------------------------------------------------------------------------------*/
        //
        //


        signCalendarView = (ZWSignCalendarView) findViewById(R.id.calendarView2);
        signCalendarView.setDateListener(new ZWSignCalendarView.DateListener() {
            @Override
            public void change(int year, int month) {
                show2.setText(String.format("%s 年 %s 月", year, month));
            }
        });

        show2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signCalendarView.selectMonth(2017, 9);
            }
        });


        findViewById(R.id.calendar_previous2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signCalendarView.showPreviousMonth();
            }
        });

        findViewById(R.id.calendar_next2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signCalendarView.showNextMonth();
            }
        });

        findViewById(R.id.tv_calendar_today2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signCalendarView.backCurrentMonth();
            }
        });

        HashSet<String> sign2 = new HashSet<>();
        sign2.add("2017-07-12");
        sign2.add("2017-07-23");
        sign2.add("2017-07-24");
        sign2.add("2017-07-25");
        sign2.add("2017-08-12");
        sign2.add("2017-08-13");
        sign2.add("2017-08-14");
        sign2.add("2017-08-15");
        sign2.add("2017-08-18");
        sign2.add("2017-08-31");
        sign2.add("2017-09-05");
        sign2.add("2017-09-07");
        sign2.add("2017-09-08");
        sign2.add("2017-09-09");
        sign2.add("2017-10-09");
        signCalendarView.setSignRecords(sign2);
    }
}

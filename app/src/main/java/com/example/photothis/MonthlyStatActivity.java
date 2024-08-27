package com.example.photothis;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class MonthlyStatActivity extends AppCompatActivity {

    private Spinner yearSpinner;
    private BarChart barChart; // BarChart 사용
    private int selectedYear;
    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_stat);

        // Firebase Database reference 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference("diary_entries");

        // 현재 시간 기준으로 연도 초기화
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);

        // Spinner 초기화
        yearSpinner = findViewById(R.id.yearSpinner);
        setupYearSpinner();

        barChart = findViewById(R.id.barChart);
        configureChart(); // 차트 기본 설정

        fetchDataFromFirebase(); // 데이터 불러오기
    }

    private void setupYearSpinner() {
        ArrayList<String> years = new ArrayList<>();
        for (int i = 2000; i <= 2030; i++) {
            years.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                years
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);

        yearSpinner.setSelection(years.indexOf(String.valueOf(selectedYear)));
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = Integer.parseInt(years.get(position));
                fetchDataFromFirebase(); // 선택한 연도에 맞게 데이터 다시 불러오기
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void fetchDataFromFirebase() {
        String startDate = selectedYear + "-01-01";
        String endDate = selectedYear + "-12-31";

        databaseReference.orderByChild("date").startAt(startDate).endAt(endDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<BarEntry> entries = new ArrayList<>();
                int[] monthCounts = new int[12]; // 월별 일기 횟수

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.child("date").getValue(String.class);
                    if (date != null) {
                        int month = Integer.parseInt(date.split("-")[1]);
                        monthCounts[month - 1]++;
                    }
                }

                for (int i = 0; i < monthCounts.length; i++) {
                    entries.add(new BarEntry(i, monthCounts[i])); // i는 0부터 시작
                }

                BarDataSet barDataSet = new BarDataSet(entries, "Monthly Stats");
                int color = ContextCompat.getColor(MonthlyStatActivity.this, R.color.colorAccent);
                barDataSet.setColor(color);
                barDataSet.setDrawValues(false);

                BarData barData = new BarData(barDataSet);
                barData.setBarWidth(0.4f); // 막대의 폭 조절

                barChart.setData(barData);
                barChart.invalidate();

                configureChart();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MonthlyStatActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void configureChart() {
        // XAxis 설정 (월 레이블)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축을 아래쪽에 배치
        xAxis.setGranularity(1f); // 1 단위로 눈금 표시
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getMonthLabels()));
        xAxis.setLabelCount(12, true);
        xAxis.setLabelRotationAngle(0f);
        xAxis.setAxisMinimum(-0.5f); // 막대가 레이블과 겹치지 않도록 최소값 설정
        xAxis.setAxisMaximum(11.5f); // 막대가 레이블과 겹치지 않도록 최대값 설정
        xAxis.setDrawGridLines(false); // 그리드 라인 비활성화
        xAxis.setDrawAxisLine(true); // 축 선 표시
        xAxis.setCenterAxisLabels(true); // 레이블을 축의 중심에 맞추기

        // YAxis 설정 (일기 작성 횟수)
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(false); // 그리드 라인 비활성화
        leftAxis.setDrawAxisLine(true); // 축 선 표시
        leftAxis.setDrawLabels(true); // 레이블 표시
        leftAxis.setAxisMaximum(35f); // 최대값 설정
        leftAxis.setAxisMinimum(0f); // 최소값 설정

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setDrawGridLines(false); // 그리드 라인 비활성화
        rightAxis.setDrawAxisLine(false); // 축 선 비활성화
        rightAxis.setDrawLabels(false); // 레이블 비활성화
        rightAxis.setEnabled(false); // 오른쪽 축 비활성화

        barChart.setDrawValueAboveBar(true); // 막대 위에 값 표시
        barChart.setFitBars(true); // 막대를 맞추기
        barChart.setNoDataText("No data available"); // 데이터 없음 텍스트
        barChart.setDrawBorders(false); // 차트 경계선 비활성화

        // 클릭 리스너 설정
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof BarEntry) {
                    BarEntry barEntry = (BarEntry) e;
                    int monthIndex = (int) barEntry.getX();
                    int count = (int) barEntry.getY();
                    Toast.makeText(MonthlyStatActivity.this, getMonthName(monthIndex) + "월의 일기 : " + count+"회", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onNothingSelected() {
                // Handle case where no value is selected
            }
        });
    }

    private String[] getMonthLabels() {
        return new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
    }

    private String getMonthName(int monthIndex) {
        String[] months = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        return months[monthIndex];
    }
}

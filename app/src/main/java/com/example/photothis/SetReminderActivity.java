package com.example.photothis;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SetReminderActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private Button saveReminderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_reminder);

        timePicker = findViewById(R.id.timePicker);
        saveReminderButton = findViewById(R.id.saveReminderButton);

        saveReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                // 알림 설정 로직을 여기에 추가
                Toast.makeText(SetReminderActivity.this, "알림이 설정되었습니다: " + hour + ":" + String.format("%02d", minute), Toast.LENGTH_SHORT).show();
            }
        });
    }
}


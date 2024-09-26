package com.example.photothis;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiarySummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_summary);

        // 제목 설정
        TextView titleTextView = findViewById(R.id.titleTextView);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1); // 한 달 전

        // 월을 1 더한 후 0 없이 출력
        int lastMonth = calendar.get(Calendar.MONTH) + 1;
        String title = calendar.get(Calendar.YEAR) + "년 " + lastMonth + "월의 일기 요약";
        titleTextView.setText(title);

        // 제목 색상 검은색으로 변경
        titleTextView.setTextColor(getResources().getColor(android.R.color.black));

        // 데이터 로드 및 요약 요청 호출 예시
        loadLastMonthDiaryEntries();
    }

    private void loadLastMonthDiaryEntries() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1); // 한 달 전

        // 정확한 날짜 형식을 위해 1을 더함 (월은 0부터 시작)
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

        // 월과 일을 항상 2자리로 맞추기
        String formattedMonth = (month < 10 ? "0" + month : String.valueOf(month));
        String startDate = year + "-" + formattedMonth + "-01"; // 시작일
        String endDate = year + "-" + formattedMonth + "-31"; // 끝일

        DatabaseReference diaryRef = FirebaseDatabase.getInstance().getReference().child("diary_entries");
        diaryRef.orderByChild("date")
                .startAt(startDate)
                .endAt(endDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        StringBuilder diaryText = new StringBuilder();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            DiaryEntry entry = snapshot.getValue(DiaryEntry.class);
                            if (entry != null) {
                                diaryText.append(entry.getText()).append(" ");
                            }
                        }
                        // 요약 요청
                        summarizeDiaryEntries(diaryText.toString().trim());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(DiarySummaryActivity.this, "데이터를 불러오는 중 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void summarizeDiaryEntries(String diaryText) {
        String apiKey = "abc"; // 발급받은 OpenAI API 키

        OpenAIService openAIService = RetrofitClient.getRetrofitInstance(apiKey)
                .create(OpenAIService.class);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "한 달치 일기를 요약해줘 조건은 다음과 같아. 1. 가장 중요해보이는 사건을 바탕으로 요약할 것. 단, 그것을 서두에서 직접적으로 언급하면 안 됨. 2. 주된 공간, 감정과 경험 위주로 요약할 것. 3. 존댓말을 사용하여 한글 200자 이내로 요약할 것: \n" + diaryText);
        messages.add(message);

        ChatCompletionRequest request = new ChatCompletionRequest("gpt-3.5-turbo", messages, 0.7);

        Call<ChatCompletionResponse> call = openAIService.getChatCompletion(request);
        call.enqueue(new Callback<ChatCompletionResponse>() {
            @Override
            public void onResponse(Call<ChatCompletionResponse> call, Response<ChatCompletionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 수정된 코드
                    String summary = response.body().getChoices().get(0).getMessage().get("content");
                    TextView summaryBox = findViewById(R.id.summaryBox);
                    summaryBox.setText(summary);
                } else {
                    Toast.makeText(DiarySummaryActivity.this, "요약 실패. 응답 코드: " + response.code(), Toast.LENGTH_SHORT).show();
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Toast.makeText(DiarySummaryActivity.this, "오류 메시지: " + errorBody, Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatCompletionResponse> call, Throwable t) {
                Toast.makeText(DiarySummaryActivity.this, "API 호출 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

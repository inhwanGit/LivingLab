package com.example.myapplication_naver;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class OnBoardingActivity extends AppCompatActivity {

    private TTS tts;
    private Button start_button;                //메인엑티비티 버튼
    private RadioButton Blind;                  //시각장애인
    private RadioButton Hearing_impaired;       //청각장애인
    private RadioButton Physically_handicapped; //지체장애인

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        Init();

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("Activity", "OnBoardingActivity");

                if(Blind.isChecked() == true){
                    intent.putExtra("DisabledType", 1);
                    Log.d("OnBoardingActivity","장애인 유형 : 시각장애인");

                } else if(Hearing_impaired.isChecked() == true){
                    intent.putExtra("DisabledType", 2);
                    Log.d("OnBoardingActivity","장애인 유형 : 청각장애인");

                } else if(Physically_handicapped.isChecked() == true){
                    intent.putExtra("DisabledType", 3);
                    Log.d("OnBoardingActivity","장애인 유형 : 지체장애인");
                }
                else
                {
                    Toast.makeText(OnBoardingActivity.this, "장애인 유형을 선택해 주세요.", Toast.LENGTH_LONG).show();
                    tts.TextToSpeech("장애인 유형을 선택해 주세요.");
                    return;
                }

                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        Blind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Blind.isChecked()){
                    Blind.setBackground(getDrawable(R.drawable.ic_circle_border));

                    Hearing_impaired.setBackground(getDrawable(R.drawable.ic_circle_gray2));
                    Physically_handicapped.setBackground(getDrawable(R.drawable.ic_circle_gray2));
                }
            }
        });

        Hearing_impaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Hearing_impaired.isChecked()){
                    Hearing_impaired.setBackground(getDrawable(R.drawable.ic_circle_border));

                    Blind.setBackground(getDrawable(R.drawable.ic_circle_gray2));
                    Physically_handicapped.setBackground(getDrawable(R.drawable.ic_circle_gray2));
                }
            }
        });

        Physically_handicapped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Physically_handicapped.isChecked()){
                    Physically_handicapped.setBackground(getDrawable(R.drawable.ic_circle_border));

                    Hearing_impaired.setBackground(getDrawable(R.drawable.ic_circle_gray2));
                    Blind.setBackground(getDrawable(R.drawable.ic_circle_gray2));
                }
            }
        });
    }

    private void Init(){
        tts = new TTS(this);
        start_button = findViewById(R.id.start_button);
        Blind = findViewById(R.id.radioButton1);
        Hearing_impaired = findViewById(R.id.radioButton2);
        Physically_handicapped = findViewById(R.id.radioButton3);
    }
}